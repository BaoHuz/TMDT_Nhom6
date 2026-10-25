package com.poolbooking.entity;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(@NotBlank String maDat) {
}
