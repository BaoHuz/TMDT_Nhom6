package com.poolbooking.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingRequest(@NotBlank String maLich, @Min(1) int soNguoi) {
}
