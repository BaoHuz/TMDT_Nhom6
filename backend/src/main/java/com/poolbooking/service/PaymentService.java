package com.poolbooking.service;

import com.poolbooking.dto.BookingDtos.PaymentRequest;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final DatLichRepository bookings; private final ThanhToanRepository payments;
    private final BookingService bookingService;
    private final VnPayGateway vnPay;
    @Autowired
    public PaymentService(DatLichRepository bookings, ThanhToanRepository payments, BookingService bookingService, VnPayGateway vnPay) {
        this.bookings = bookings; this.payments = payments; this.bookingService = bookingService; this.vnPay = vnPay;
    }
    public PaymentService(DatLichRepository bookings, ThanhToanRepository payments, BookingService bookingService) {
        this(bookings, payments, bookingService, new VnPayGateway(org.springframework.web.client.RestClient.builder()));
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public String createVnPayUrl(String username, String bookingId, String ipAddress) {
        DatLich owned = bookingService.owned(username, bookingId);
        DatLich booking = bookingService.lockForPayment(owned.maDat);
        if (booking.trangThai != TrangThaiDatLich.CHO_THANH_TOAN)
            throw new BusinessException("Đơn không ở trạng thái chờ thanh toán");
        String txnRef = "VN" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        ThanhToan payment = new ThanhToan();
        payment.maTt = "TT-" + UUID.randomUUID();
        payment.maDat = booking.maDat;
        payment.maGiaoDich = txnRef;
        payment.soTien = booking.tongTien;
        payment.phuongThuc = "VNPAY";
        payment.trangThai = TrangThaiThanhToan.CHO_XU_LY;
        payments.save(payment);
        return vnPay.createPaymentUrl(txnRef, booking.tongTien, "Thanh toan don " + booking.maDat,
                ipAddress, now, booking.hanThanhToan);
    }

    @Transactional
    public Map<String, String> handleVnPayIpn(Map<String, String> params) {
        if (!vnPay.verify(params)) return ipnResult("97", "Invalid signature");
        ThanhToan payment = payments.findByMaGiaoDich(params.get("vnp_TxnRef")).orElse(null);
        if (payment == null) return ipnResult("01", "Order not found");
        if (payment.trangThai == TrangThaiThanhToan.THANH_CONG) return ipnResult("02", "Order already confirmed");
        try {
            BigDecimal amount = new BigDecimal(params.getOrDefault("vnp_Amount", "0")).movePointLeft(2);
            if (amount.compareTo(payment.soTien) != 0) return ipnResult("04", "Invalid amount");
        } catch (NumberFormatException ex) { return ipnResult("04", "Invalid amount"); }

        if (bookings.findById(payment.maDat).isEmpty()) return ipnResult("01", "Order not found");
        DatLich booking = bookingService.lockForPayment(payment.maDat);
        payment=payments.lockByMaGiaoDich(params.get("vnp_TxnRef")).orElse(null);
        if (payment == null) return ipnResult("01", "Order not found");
        if (payment.trangThai == TrangThaiThanhToan.THANH_CONG) return ipnResult("02", "Order already confirmed");
        boolean success = "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus"));
        if (success && booking.trangThai == TrangThaiDatLich.CHO_THANH_TOAN
                && booking.hanThanhToan != null && booking.hanThanhToan.isAfter(LocalDateTime.now())) {
            payment.trangThai = TrangThaiThanhToan.THANH_CONG;
            payment.vnpayMaGiaoDichGoc = params.get("vnp_TransactionNo");
            payment.vnpayThoiDiemGiaoDichGoc = params.get("vnp_PayDate");
            booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;
            booking.maVeQr = "QR-" + booking.maDat;
            bookings.save(booking);
        } else {
            payment.trangThai = TrangThaiThanhToan.THAT_BAI;
        }
        payments.save(payment);
        return ipnResult("00", "Received");
    }

    public boolean verifyVnPayReturn(Map<String, String> params) { return vnPay.verify(params); }

    @Transactional(readOnly = true)
    public String bookingIdForVnPayReturn(String txnRef) {
        return payments.findByMaGiaoDich(txnRef).map(x -> x.maDat).orElse(null);
    }

    private Map<String, String> ipnResult(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public DatLich pay(String username, PaymentRequest request) {
        DatLich owned = bookingService.owned(username, request.maDat());
        DatLich booking = bookingService.lockForPayment(owned.maDat);
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
