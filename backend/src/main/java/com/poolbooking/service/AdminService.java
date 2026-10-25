package com.poolbooking.service;

import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.KhuVuc;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.ScheduleRequest;
import com.poolbooking.entity.ServiceRequest;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.enums.TrangThaiThanhToan;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.CheckInRepository;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.DichVuRepository;
import com.poolbooking.repository.KhuVucRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.ThanhToanRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final DatLichRepository bookingRepository;
    private final ThanhToanRepository paymentRepository;
    private final CheckInRepository checkInRepository;
    private final LichDichVuRepository scheduleRepository;
    private final DichVuRepository serviceRepository;
    private final KhuVucRepository areaRepository;

    public AdminService(DatLichRepository bookingRepository, ThanhToanRepository paymentRepository,
            CheckInRepository checkInRepository, LichDichVuRepository scheduleRepository,
            DichVuRepository serviceRepository, KhuVucRepository areaRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.checkInRepository = checkInRepository;
        this.scheduleRepository = scheduleRepository;
        this.serviceRepository = serviceRepository;
        this.areaRepository = areaRepository;
    }

    public List<DatLich> getBookings() {
        return bookingRepository.findAll();
    }

    public List<DichVu> getServices() {
        return serviceRepository.findAll();
    }

    public List<KhuVuc> getAreas() {
        return areaRepository.findAll();
    }

    public DichVu addService(ServiceRequest request) {
        DichVu service = new DichVu();
        service.maDv = "DV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        service.tenDv = request.tenDv();
        service.donGia = request.donGia();
        service.thoiLuongPhut = request.thoiLuongPhut();
        return serviceRepository.save(service);
    }

    public DichVu updateService(String id, ServiceRequest request) {
        DichVu service = serviceRepository.findById(id).orElseThrow();
        service.tenDv = request.tenDv();
        service.donGia = request.donGia();
        service.thoiLuongPhut = request.thoiLuongPhut();
        return serviceRepository.save(service);
    }

    public List<LichDichVu> getSchedules() {
        return scheduleRepository.findAll();
    }

    public LichDichVu addSchedule(ScheduleRequest request) {
        var area = areaRepository.findById(request.maKhuVuc()).orElseThrow();
        if (serviceRepository.findById(request.maDv()).isEmpty()) {
            throw new BusinessException("Không tìm thấy dịch vụ");
        }
        if (request.ngay().isBefore(LocalDate.now())
                || (request.ngay().isEqual(LocalDate.now())
                        && !LocalDateTime.of(request.ngay(), request.gioBatDau()).isAfter(LocalDateTime.now()))) {
            throw new BusinessException("Ca bơi phải được tạo trong tương lai");
        }
        if (!request.gioBatDau().isBefore(request.gioKetThuc())) {
            throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
        }
        if (request.sucChua() > area.sucChua) {
            throw new BusinessException("Sức chứa ca vượt sức chứa khu vực");
        }
        boolean overlap = scheduleRepository.findByNgayAndTrangThaiInOrderByMaLichAsc(request.ngay(),
                        List.of(TrangThaiLich.MO, TrangThaiLich.HET_CHO)).stream()
                .anyMatch(existing -> existing.maKhuVuc.equals(request.maKhuVuc())
                        && request.gioBatDau().isBefore(existing.gioKetThuc)
                        && request.gioKetThuc().isAfter(existing.gioBatDau));
        if (overlap) {
            throw new BusinessException("Khu vực đã có ca bơi trùng khung giờ");
        }

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        schedule.maDv = request.maDv();
        schedule.maKhuVuc = request.maKhuVuc();
        schedule.ngay = request.ngay();
        schedule.gioBatDau = request.gioBatDau();
        schedule.gioKetThuc = request.gioKetThuc();
        schedule.sucChua = request.sucChua();
        schedule.soChoCon = request.sucChua();
        return scheduleRepository.save(schedule);
    }

    public LichDichVu closeSchedule(String id) {
        LichDichVu schedule = scheduleRepository.findById(id).orElseThrow();
        schedule.trangThai = TrangThaiLich.DONG;
        return scheduleRepository.save(schedule);
    }

    public Map<String, Object> dashboard() {
        BigDecimal revenue = paymentRepository.findByTrangThai(TrangThaiThanhToan.THANH_CONG).stream()
                .map(payment -> payment.soTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of(
                "tongDon", bookingRepository.count(),
                "doanhThu", revenue,
                "luotCheckIn", checkInRepository.count(),
                "caDangMo", scheduleRepository.findByNgayAndTrangThai(LocalDate.now(), TrangThaiLich.MO).size());
    }
}
