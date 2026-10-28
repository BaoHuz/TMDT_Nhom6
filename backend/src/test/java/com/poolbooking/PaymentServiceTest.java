package com.poolbooking;

import com.poolbooking.dto.BookingDtos.PaymentRequest;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import com.poolbooking.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private DatLichRepository bookings;
    @Mock private ThanhToanRepository payments;
    @Mock private BookingService bookingService;
    @Mock private VnPayGateway vnPay;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(bookings, payments, bookingService, vnPay);
    }

    @Test
    void paymentFailure_DoesNotGenerateQR_AndSavesFailedPayment() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-FAIL";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.CHO_THANH_TOAN;
        booking.maVeQr = null;

        when(bookingService.owned("customer1", "DL-FAIL")).thenReturn(booking);
        when(bookingService.lockForPayment("DL-FAIL")).thenReturn(booking);

        PaymentRequest request = new PaymentRequest("DL-FAIL", true); // simulateFailure = true

        assertThatThrownBy(() -> paymentService.pay("customer1", request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Thanh toán mô phỏng thất bại");

        // Verify booking status was NOT changed to DA_XAC_NHAN and QR was NOT generated
        assertThat(booking.trangThai).isEqualTo(TrangThaiDatLich.CHO_THANH_TOAN);
        assertThat(booking.maVeQr).isNull();

        // Verify failed payment transaction was recorded
        ArgumentCaptor<ThanhToan> paymentCaptor = ArgumentCaptor.forClass(ThanhToan.class);
        verify(payments).save(paymentCaptor.capture());
        ThanhToan savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.trangThai).isEqualTo(TrangThaiThanhToan.THAT_BAI);
        assertThat(savedPayment.maDat).isEqualTo("DL-FAIL");
    }

    @Test
    void paymentSuccess_GeneratesQR_AndConfirmsBooking() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-SUCCESS";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.CHO_THANH_TOAN;

        when(bookingService.owned("customer1", "DL-SUCCESS")).thenReturn(booking);
        when(bookingService.lockForPayment("DL-SUCCESS")).thenReturn(booking);
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRequest request = new PaymentRequest("DL-SUCCESS", false);
        DatLich confirmed = paymentService.pay("customer1", request);

        assertThat(confirmed.trangThai).isEqualTo(TrangThaiDatLich.DA_XAC_NHAN);
        assertThat(confirmed.maVeQr).isEqualTo("QR-DL-SUCCESS");

        ArgumentCaptor<ThanhToan> paymentCaptor = ArgumentCaptor.forClass(ThanhToan.class);
        verify(payments).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().trangThai).isEqualTo(TrangThaiThanhToan.THANH_CONG);
    }

    @Test
    void successfulVnPayIpnStoresOriginalTransactionNumberAndDateForRefunds() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-IPN";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.CHO_THANH_TOAN;
        booking.hanThanhToan = LocalDateTime.now().plusMinutes(10);

        ThanhToan payment = new ThanhToan();
        payment.maTt = "TT-IPN";
        payment.maDat = booking.maDat;
        payment.maGiaoDich = "VN-TXN-IPN";
        payment.soTien = booking.tongTien;
        payment.trangThai = TrangThaiThanhToan.CHO_XU_LY;

        Map<String, String> ipn = Map.of(
            "vnp_TxnRef", "VN-TXN-IPN",
            "vnp_Amount", "10000000",
            "vnp_ResponseCode", "00",
            "vnp_TransactionStatus", "00",
            "vnp_TransactionNo", "987654321",
            "vnp_PayDate", "20261028213045"
        );
        when(vnPay.verify(ipn)).thenReturn(true);
        when(payments.findByMaGiaoDich("VN-TXN-IPN")).thenReturn(Optional.of(payment));
        when(bookings.findById("DL-IPN")).thenReturn(Optional.of(booking));
        when(bookingService.lockForPayment("DL-IPN")).thenReturn(booking);
        when(payments.lockByMaGiaoDich("VN-TXN-IPN")).thenReturn(Optional.of(payment));

        Map<String, String> result = paymentService.handleVnPayIpn(ipn);

        assertThat(result.get("RspCode")).isEqualTo("00");
        assertThat(payment.vnpayMaGiaoDichGoc).isEqualTo("987654321");
        assertThat(payment.vnpayThoiDiemGiaoDichGoc).isEqualTo("20261028213045");
        assertThat(payment.trangThai).isEqualTo(TrangThaiThanhToan.THANH_CONG);
        verify(payments).save(payment);
    }
}
