package com.poolbooking.service;

import com.poolbooking.entity.BookingRequest;
import com.poolbooking.entity.BookingView;
import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.KhuVuc;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.exception.ResourceNotFoundException;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.DichVuRepository;
import com.poolbooking.repository.KhuVucRepository;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class BookingService {
    public static final int HOLD_MINUTES = 5;

    private final DatLichRepository bookingRepository;
    private final LichDichVuRepository scheduleRepository;
    private final DichVuRepository serviceRepository;
    private final KhuVucRepository areaRepository;
    private final KhachHangRepository customerRepository;
    private final TaiKhoanRepository userRepository;

    public BookingService(DatLichRepository bookingRepository, LichDichVuRepository scheduleRepository,
            DichVuRepository serviceRepository, KhuVucRepository areaRepository,
            KhachHangRepository customerRepository, TaiKhoanRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.serviceRepository = serviceRepository;
        this.areaRepository = areaRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public DatLich create(String userName, BookingRequest request) {
        LichDichVu schedule = scheduleRepository.lockById(request.maLich())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        expirePendingBookings(schedule);
        LocalDateTime start = LocalDateTime.of(schedule.ngay, schedule.gioBatDau);
        if (!start.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Ca bơi đã bắt đầu hoặc đã qua");
        }
        if (schedule.trangThai != TrangThaiLich.MO) {
            throw new BusinessException("Ca bơi không còn mở");
        }
        if (request.soNguoi() > schedule.soChoCon) {
            throw new BusinessException("Không đủ vé còn trống. Vui lòng chọn số lượng khác");
        }

        DatLich booking = new DatLich();
        booking.maDat = newId("DL-");
        TaiKhoan user = userRepository.findByTenDangNhap(userName).orElseThrow();
        KhachHang customer = customerRepository.findByMaTk(user.maTk)
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có hồ sơ khách hàng"));
        booking.maKh = customer.maKh;
        booking.maLich = schedule.maLich;
        booking.soNguoi = request.soNguoi();
        DichVu service = serviceRepository.findById(schedule.maDv).orElseThrow();
        booking.donGiaTaiDat = service.donGia;
        booking.tongTien = booking.donGiaTaiDat.multiply(BigDecimal.valueOf(booking.soNguoi));
        schedule.soChoCon -= booking.soNguoi;
        if (schedule.soChoCon == 0) {
            schedule.trangThai = TrangThaiLich.HET_CHO;
        }
        scheduleRepository.save(schedule);
        return bookingRepository.save(booking);
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public List<BookingView> my(String userName) {
        String customerId = customerId(userName);
        List<DatLich> bookings = bookingRepository.findByMaKhOrderByNgayDatDesc(customerId);
        Set<String> scheduleIds = new TreeSet<>();
        LocalDateTime expiryCutoff = LocalDateTime.now().minusMinutes(HOLD_MINUTES);
        bookings.stream()
                .filter(booking -> booking.trangThai == TrangThaiDatLich.CHO_THANH_TOAN
                        && !booking.ngayDat.isAfter(expiryCutoff))
                .forEach(booking -> scheduleIds.add(booking.maLich));
        scheduleIds.forEach(scheduleId -> scheduleRepository.lockById(scheduleId)
                .ifPresent(this::expirePendingBookings));
        return bookingRepository.findByMaKhOrderByNgayDatDesc(customerId).stream()
                .map(this::view)
                .toList();
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public void cancel(String userName, String id) {
        DatLich initial = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        LichDichVu schedule = scheduleRepository.lockById(initial.maLich).orElseThrow();
        expirePendingBookings(schedule);
        DatLich booking = bookingRepository.lockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        requireOwner(userName, booking);
        if (booking.trangThai != TrangThaiDatLich.CHO_THANH_TOAN) {
            throw new BusinessException("Chỉ có thể hủy đơn đang chờ thanh toán");
        }
        if (!LocalDateTime.of(schedule.ngay, schedule.gioBatDau).isAfter(LocalDateTime.now())) {
            throw new BusinessException("Đã quá giờ bắt đầu");
        }
        booking.trangThai = TrangThaiDatLich.DA_HUY;
        schedule.soChoCon = Math.min(schedule.sucChua, schedule.soChoCon + booking.soNguoi);
        reopenIfSeatsAvailable(schedule);
        scheduleRepository.save(schedule);
        bookingRepository.save(booking);
    }

    public DatLich owned(String userName, String id) {
        DatLich booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        requireOwner(userName, booking);
        return booking;
    }

    public void expirePendingBookings(LichDichVu schedule) {
        LocalDateTime now = LocalDateTime.now();
        List<DatLich> pending = bookingRepository.findByMaLichAndTrangThaiOrderByNgayDatAsc(
                schedule.maLich, TrangThaiDatLich.CHO_THANH_TOAN);
        int releasedSeats = 0;
        for (DatLich booking : pending) {
            if (!booking.ngayDat.plusMinutes(HOLD_MINUTES).isAfter(now)) {
                booking.trangThai = TrangThaiDatLich.HET_HAN;
                bookingRepository.save(booking);
                releasedSeats += booking.soNguoi;
            }
        }
        if (releasedSeats > 0) {
            schedule.soChoCon = Math.min(schedule.sucChua, schedule.soChoCon + releasedSeats);
            reopenIfSeatsAvailable(schedule);
            scheduleRepository.save(schedule);
        }
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void expireStaleHolds() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(HOLD_MINUTES);
        Set<String> scheduleIds = new TreeSet<>();
        bookingRepository.findByTrangThaiAndNgayDatBefore(TrangThaiDatLich.CHO_THANH_TOAN, cutoff)
                .forEach(booking -> scheduleIds.add(booking.maLich));
        scheduleIds.forEach(scheduleId -> scheduleRepository.lockById(scheduleId)
                .ifPresent(this::expirePendingBookings));
    }

    public BookingView view(DatLich booking) {
        LichDichVu schedule = scheduleRepository.findById(booking.maLich)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        DichVu service = serviceRepository.findById(schedule.maDv)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));
        KhuVuc area = areaRepository.findById(schedule.maKhuVuc)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khu vực"));
        return new BookingView(
                booking.maDat,
                booking.maLich,
                booking.maVeQr,
                booking.soNguoi,
                booking.donGiaTaiDat,
                booking.tongTien,
                booking.ngayDat,
                booking.trangThai == TrangThaiDatLich.CHO_THANH_TOAN
                        ? booking.ngayDat.plusMinutes(HOLD_MINUTES)
                        : null,
                LocalDateTime.of(schedule.ngay, schedule.gioKetThuc),
                booking.trangThai,
                service.tenDv,
                area.tenKhuVuc,
                schedule.ngay,
                schedule.gioBatDau,
                schedule.gioKetThuc);
    }

    private void reopenIfSeatsAvailable(LichDichVu schedule) {
        if (schedule.trangThai == TrangThaiLich.HET_CHO && schedule.soChoCon > 0) {
            schedule.trangThai = TrangThaiLich.MO;
        }
    }

    private void requireOwner(String userName, DatLich booking) {
        if (!booking.maKh.equals(customerId(userName))) {
            throw new BusinessException("Bạn không có quyền truy cập đơn này");
        }
    }

    private String customerId(String userName) {
        TaiKhoan user = userRepository.findByTenDangNhap(userName).orElseThrow();
        KhachHang customer = customerRepository.findByMaTk(user.maTk)
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có hồ sơ khách hàng"));
        if (!"HOAT_DONG".equals(customer.trangThai)) {
            throw new BusinessException("Tài khoản khách hàng đã bị khóa");
        }
        return customer.maKh;
    }

    private String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
