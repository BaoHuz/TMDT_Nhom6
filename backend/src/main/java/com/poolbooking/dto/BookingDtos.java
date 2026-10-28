package com.poolbooking.dto;

import jakarta.validation.constraints.*;

public final class BookingDtos {
    private BookingDtos() {}
    public record BookingRequest(@NotBlank String maLich, @Min(1) int soNguoi) {}
    public record PaymentRequest(@NotBlank String maDat, Boolean simulateFailure) {}
    public record PaymentLinkResponse(String paymentUrl) {}
    public record CheckInRequest(@NotBlank String maVeQr) {}
}
