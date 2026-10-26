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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private DatLichRepository bookings;
    @Mock private ThanhToanRepository payments;
    @Mock private BookingService bookingService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(bookings, payments, bookingService);
    }

    @Test
    void paymentFailure_DoesNotGenerateQR_AndSavesFailedPayment() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-FAIL";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.CHO_THANH_TOAN;
        booking.maVeQr = null;

        when(bookingService.owned("customer1", "DL-FAIL")).thenReturn(booking);
        when(bookings.lockById("DL-FAIL")).thenReturn(Optional.of(booking));

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
        when(bookings.lockById("DL-SUCCESS")).thenReturn(Optional.of(booking));
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRequest request = new PaymentRequest("DL-SUCCESS", false);
        DatLich confirmed = paymentService.pay("customer1", request);

        assertThat(confirmed.trangThai).isEqualTo(TrangThaiDatLich.DA_XAC_NHAN);
        assertThat(confirmed.maVeQr).isEqualTo("QR-DL-SUCCESS");

        ArgumentCaptor<ThanhToan> paymentCaptor = ArgumentCaptor.forClass(ThanhToan.class);
        verify(payments).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().trangThai).isEqualTo(TrangThaiThanhToan.THANH_CONG);
    }
}
