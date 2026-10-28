package com.poolbooking.service;

import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiThanhToan;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.exception.ResourceNotFoundException;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.ThanhToanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminRefundService {
    private static final String PENDING = "CHO_HOAN";
    private static final String PROCESSING = "DANG_XU_LY";
    private final DatLichRepository bookings;
    private final LichDichVuRepository schedules;
    private final ThanhToanRepository payments;
    private final VnPayGateway vnPay;
    private final TransactionTemplate transactions;

    public AdminRefundService(DatLichRepository bookings, LichDichVuRepository schedules,
                              ThanhToanRepository payments, VnPayGateway vnPay,
                              PlatformTransactionManager transactionManager) {
        this.bookings = bookings;
        this.schedules = schedules;
        this.payments = payments;
        this.vnPay = vnPay;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public Map<String, Object> refund(String bookingId, String adminUsername, String ipAddress) {
        vnPay.validateConfiguration();
        RefundAttempt attempt = transactions.execute(status -> beginRefund(bookingId));
        if (attempt == null) throw new IllegalStateException("Không thể tạo yêu cầu hoàn tiền");

        Map<String, Object> response;
        try {
            response = vnPay.refund(new VnPayGateway.RefundCommand(
                attempt.requestId(),
                attempt.transactionReference(),
                attempt.amount(),
                attempt.transactionNumber() == null || attempt.transactionNumber().isBlank()
                    ? "0" : attempt.transactionNumber(),
                attempt.transactionDate(),
                attempt.amount().compareTo(attempt.originalAmount()) == 0,
                adminUsername,
                ipAddress,
                "Hoan tien don " + bookingId,
                LocalDateTime.now()
            ));
        } catch (RestClientException ex) {
            String note = "Không nhận được xác nhận từ VNPay; yêu cầu đang được đối soát. Không gửi lại.";
            transactions.executeWithoutResult(status -> recordUncertainResult(attempt.paymentId(), note));
            throw new BusinessException(note);
        }

        if (!vnPay.verifyRefundResponse(response)) {
            String note = "Không hợp lệ chữ ký phản hồi hoàn tiền VNPay; yêu cầu đang được đối soát.";
            transactions.executeWithoutResult(status -> recordUncertainResult(attempt.paymentId(), note));
            throw new BusinessException(note);
        }
        String responseCode = value(response, "vnp_ResponseCode");
        if (!"00".equals(responseCode)) {
            String message = response == null ? "VNPay trả về phản hồi rỗng"
                : value(response, "vnp_Message");
            String note = limit("VNPay từ chối yêu cầu hoàn tiền"
                + (message == null || message.isBlank() ? "" : ": " + message));
            transactions.executeWithoutResult(status -> rejectRefund(attempt.paymentId(), note));
            throw new BusinessException(note);
        }

        String refundReference = responseReference(response);
        if (refundReference == null || refundReference.isBlank() || "0".equals(refundReference)) {
            String note = "VNPay báo thành công nhưng thiếu mã giao dịch hoàn; yêu cầu đang được đối soát.";
            transactions.executeWithoutResult(status -> recordUncertainResult(attempt.paymentId(), note));
            throw new BusinessException(note);
        }

        transactions.executeWithoutResult(status ->
            completeRefund(attempt, refundReference));
        return Map.of(
            "trangThaiHoanTien", attempt.amount().compareTo(attempt.originalAmount()) == 0
                ? "HOAN_TOAN" : "HOAN_MOT_PHAN",
            "soTienHoanThucTe", attempt.amount(),
            "maGiaoDichHoan", refundReference
        );
    }

    private RefundAttempt beginRefund(String bookingId) {
        DatLich existing = bookings.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        LichDichVu schedule = schedules.lockById(existing.maLich)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        DatLich booking = bookings.lockById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        if (booking.trangThai != TrangThaiDatLich.DA_HUY) {
            throw new BusinessException("Chỉ hoàn tiền cho đơn đã hủy");
        }

        List<ThanhToan> successfulPayments = payments.lockSuccessfulPayments(
            bookingId, TrangThaiThanhToan.THANH_CONG);
        ThanhToan payment = successfulPayments.stream().findFirst()
            .orElseThrow(() -> new BusinessException("Đơn chưa có giao dịch thanh toán thành công"));
        if (!"VNPAY".equalsIgnoreCase(payment.phuongThuc)) {
            throw new BusinessException("Giao dịch này không thanh toán qua VNPay, không thể hoàn tự động");
        }
        if (!PENDING.equals(payment.trangThaiHoan)) {
            throw new BusinessException(switch (payment.trangThaiHoan == null ? "" : payment.trangThaiHoan) {
                case PROCESSING -> "Yêu cầu hoàn đang được xử lý/đối soát, không thể gửi trùng";
                case "HOAN_TOAN", "HOAN_MOT_PHAN" -> "Giao dịch đã được hoàn tiền";
                default -> "Giao dịch không có yêu cầu hoàn đang chờ xử lý";
            });
        }
        if (payment.maGiaoDich == null || payment.maGiaoDich.isBlank()
                || payment.vnpayThoiDiemGiaoDichGoc == null || payment.vnpayThoiDiemGiaoDichGoc.isBlank()) {
            throw new BusinessException("Thiếu ngày giao dịch gốc VNPay; không thể hoàn tự động");
        }
        BigDecimal amount = payment.soTienYeuCauHoan;
        if (amount == null || amount.signum() <= 0 || amount.compareTo(payment.soTien) > 0) {
            throw new BusinessException("Số tiền yêu cầu hoàn không hợp lệ");
        }

        payment.trangThaiHoan = PROCESSING;
        payment.ghiChuHoan = null;
        payments.save(payment);
        String requestId = UUID.nameUUIDFromBytes(
            (payment.maTt + ":" + payment.maYeuCauHoan).getBytes(StandardCharsets.UTF_8)
        ).toString().replace("-", "");
        return new RefundAttempt(payment.maTt, requestId, payment.maGiaoDich, amount,
            payment.soTien, payment.vnpayMaGiaoDichGoc, payment.vnpayThoiDiemGiaoDichGoc);
    }

    private void rejectRefund(String paymentId, String note) {
        ThanhToan payment = lockPayment(paymentId);
        if (PROCESSING.equals(payment.trangThaiHoan)) {
            payment.trangThaiHoan = PENDING;
            payment.ghiChuHoan = note;
            payments.save(payment);
        }
    }

    private void recordUncertainResult(String paymentId, String note) {
        ThanhToan payment = lockPayment(paymentId);
        if (PROCESSING.equals(payment.trangThaiHoan)) {
            payment.ghiChuHoan = limit(note);
            payments.save(payment);
        }
    }

    private void completeRefund(RefundAttempt attempt, String refundReference) {
        ThanhToan payment = lockPayment(attempt.paymentId());
        if (!PROCESSING.equals(payment.trangThaiHoan)) {
            throw new BusinessException("Trạng thái yêu cầu đã thay đổi; cần đối soát giao dịch VNPay");
        }
        payment.trangThaiHoan = attempt.amount().compareTo(attempt.originalAmount()) == 0
            ? "HOAN_TOAN" : "HOAN_MOT_PHAN";
        payment.soTienHoan = attempt.amount();
        payment.thoiGianHoan = LocalDateTime.now();
        payment.maGiaoDichHoan = refundReference;
        payment.ghiChuHoan = null;
        payments.save(payment);
    }

    private ThanhToan lockPayment(String paymentId) {
        return payments.lockById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán"));
    }

    private String responseReference(Map<String, Object> response) {
        String responseId = value(response, "vnp_ResponseId");
        return responseId == null || responseId.isBlank()
            ? value(response, "vnp_TransactionNo") : responseId;
    }

    private String value(Map<String, Object> response, String key) {
        Object value = response.get(key);
        return value == null ? null : value.toString();
    }

    private String limit(String value) {
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private record RefundAttempt(String paymentId, String requestId, String transactionReference,
                                 BigDecimal amount, BigDecimal originalAmount,
                                 String transactionNumber, String transactionDate) {}
}
