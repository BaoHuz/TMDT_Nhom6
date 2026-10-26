package com.poolbooking.service;

import com.poolbooking.dto.BookingDtos.PaymentRequest;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PaymentService {
    private final DatLichRepository bookings; private final ThanhToanRepository payments;
    private final BookingService bookingService;
    public PaymentService(DatLichRepository bookings, ThanhToanRepository payments, BookingService bookingService) {
        this.bookings = bookings; this.payments = payments; this.bookingService = bookingService;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public DatLich pay(String username, PaymentRequest request) {
        DatLich owned = bookingService.owned(username, request.maDat());
        DatLich booking = bookings.lockById(owned.maDat).orElseThrow();
        if (booking.trangThai != TrangThaiDatLich.CHO_THANH_TOAN)
            throw new BusinessException("Đơn không ở trạng thái chờ thanh toán");
        ThanhToan payment = new ThanhToan();
        payment.maTt = "TT-" + UUID.randomUUID(); payment.maDat = booking.maDat;
        payment.maGiaoDich = "SIM-" + UUID.randomUUID(); payment.soTien = booking.tongTien;
        payment.phuongThuc = "MO_PHONG";
        if (Boolean.TRUE.equals(request.simulateFailure())) {
            payment.trangThai = TrangThaiThanhToan.THAT_BAI;
            payments.save(payment);
            throw new BusinessException("Thanh toán mô phỏng thất bại. Đơn vẫn đang chờ thanh toán");
        }
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;
        payments.save(payment);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN; booking.maVeQr = "QR-" + booking.maDat;
        return bookings.save(booking);
    }
}
