package com.poolbooking.service;

import com.poolbooking.entity.BookingView;
import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.PaymentRequest;
import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiThanhToan;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.ThanhToanRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final DatLichRepository bookingRepository;
    private final ThanhToanRepository paymentRepository;
    private final LichDichVuRepository scheduleRepository;
    private final BookingService bookingService;

    public PaymentService(DatLichRepository bookingRepository, ThanhToanRepository paymentRepository,
            LichDichVuRepository scheduleRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingService = bookingService;
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public BookingView pay(String userName, PaymentRequest request) {
        DatLich initial = bookingRepository.findById(request.maDat())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng"));
        LichDichVu schedule = scheduleRepository.lockById(initial.maLich)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca bơi"));
        bookingService.expirePendingBookings(schedule);
        DatLich booking = bookingRepository.lockById(request.maDat())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng"));
        bookingService.owned(userName, booking.maDat);
        if (booking.trangThai != TrangThaiDatLich.CHO_THANH_TOAN) {
            throw new BusinessException(booking.trangThai == TrangThaiDatLich.HET_HAN
                    ? "Đơn đã hết hạn giữ chỗ. Vui lòng đặt lại"
                    : "Đơn không ở trạng thái chờ thanh toán");
        }
        if (!LocalDateTime.of(schedule.ngay, schedule.gioBatDau).isAfter(LocalDateTime.now())) {
            throw new BusinessException("Ca bơi đã bắt đầu, không thể thanh toán");
        }

        ThanhToan payment = new ThanhToan();
        payment.maThanhToan = "TT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        payment.maDat = booking.maDat;
        payment.maGiaoDich = "SIM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        payment.soTien = booking.tongTien;
        payment.phuongThuc = "MO_PHONG";
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;
        paymentRepository.save(payment);

        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;
        booking.maVeQr = "QR-" + booking.maDat;
        bookingRepository.save(booking);
        return bookingService.view(booking);
    }
}
