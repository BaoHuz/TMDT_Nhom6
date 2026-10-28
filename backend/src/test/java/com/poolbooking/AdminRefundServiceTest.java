package com.poolbooking;

import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiThanhToan;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.ThanhToanRepository;
import com.poolbooking.service.AdminRefundService;
import com.poolbooking.service.VnPayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.client.ResourceAccessException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRefundServiceTest {
    @Mock private DatLichRepository bookings;
    @Mock private LichDichVuRepository schedules;
    @Mock private ThanhToanRepository payments;
    @Mock private VnPayGateway vnPay;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private TransactionStatus transactionStatus;

    private AdminRefundService service;
    private DatLich booking;
    private LichDichVu schedule;
    private ThanhToan payment;

    @BeforeEach
    void setUp() {
        service = new AdminRefundService(bookings, schedules, payments, vnPay, transactionManager);
        booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maLich = "L-01";
        booking.trangThai = TrangThaiDatLich.DA_HUY;
        schedule = new LichDichVu();
        schedule.maLich = "L-01";
        schedule.ngay = LocalDate.now().plusDays(1);
        schedule.gioBatDau = LocalTime.of(9, 0);
        payment = new ThanhToan();
        payment.maTt = "TT-01";
        payment.maDat = "DL-01";
        payment.maGiaoDich = "VN-TXN-REF";
        payment.phuongThuc = "VNPAY";
        payment.soTien = BigDecimal.valueOf(100000);
        payment.soTienYeuCauHoan = BigDecimal.valueOf(50000);
        payment.maYeuCauHoan = "REF-REQ-01";
        payment.vnpayMaGiaoDichGoc = "987654321";
        payment.vnpayThoiDiemGiaoDichGoc = "20261028123045";
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;
        payment.trangThaiHoan = "CHO_HOAN";

        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(bookings.findById("DL-01")).thenReturn(Optional.of(booking));
        when(schedules.lockById("L-01")).thenReturn(Optional.of(schedule));
        when(bookings.lockById("DL-01")).thenReturn(Optional.of(booking));
        when(payments.lockSuccessfulPayments("DL-01", TrangThaiThanhToan.THANH_CONG))
            .thenReturn(List.of(payment));
        lenient().when(payments.lockById("TT-01")).thenReturn(Optional.of(payment));
    }

    @Test
    void recordsCompletionOnlyAfterVnPayReturnsSuccess() {
        when(vnPay.refund(any())).thenReturn(Map.of(
            "vnp_ResponseCode", "00",
            "vnp_ResponseId", "VN-REFUND-01"
        ));
        when(vnPay.verifyRefundResponse(any())).thenReturn(true);

        Map<String, Object> result = service.refund("DL-01", "admin", "127.0.0.1");

        assertThat(result.get("trangThaiHoanTien")).isEqualTo("HOAN_MOT_PHAN");
        assertThat(payment.trangThaiHoan).isEqualTo("HOAN_MOT_PHAN");
        assertThat(payment.soTienHoan).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(payment.thoiGianHoan).isNotNull();
        assertThat(payment.maGiaoDichHoan).isEqualTo("VN-REFUND-01");
        assertThat(payment.ghiChuHoan).isNull();
        verify(vnPay).refund(any());
        verify(payments, times(2)).save(payment);
    }

    @Test
    void rejectsRefundWhenVnPayDeclinesAndKeepsRequestRetryable() {
        when(vnPay.refund(any())).thenReturn(Map.of(
            "vnp_ResponseCode", "91",
            "vnp_Message", "Transaction not found"
        ));
        when(vnPay.verifyRefundResponse(any())).thenReturn(true);

        assertThatThrownBy(() -> service.refund("DL-01", "admin", "127.0.0.1"))
            .isInstanceOf(com.poolbooking.exception.BusinessException.class)
            .hasMessageContaining("VNPay từ chối");

        assertThat(payment.trangThaiHoan).isEqualTo("CHO_HOAN");
        assertThat(payment.ghiChuHoan).contains("Transaction not found");
        assertThat(payment.soTienHoan).isNull();
        assertThat(payment.maGiaoDichHoan).isNull();
    }

    @Test
    void keepsRefundInReconciliationStateWhenGatewayResponseIsUnknown() {
        when(vnPay.refund(any())).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> service.refund("DL-01", "admin", "127.0.0.1"))
            .isInstanceOf(com.poolbooking.exception.BusinessException.class)
            .hasMessageContaining("đang được đối soát");

        assertThat(payment.trangThaiHoan).isEqualTo("DANG_XU_LY");
        assertThat(payment.ghiChuHoan).contains("Không nhận được xác nhận");
        assertThat(payment.soTienHoan).isNull();
        assertThat(payment.maGiaoDichHoan).isNull();
    }

    @Test
    void refusesToCallVnPayWhenOriginalPaymentDateIsMissing() {
        payment.vnpayThoiDiemGiaoDichGoc = null;

        assertThatThrownBy(() -> service.refund("DL-01", "admin", "127.0.0.1"))
            .isInstanceOf(com.poolbooking.exception.BusinessException.class)
            .hasMessageContaining("Thiếu ngày giao dịch gốc");

        verify(vnPay, never()).refund(any());
    }
}
