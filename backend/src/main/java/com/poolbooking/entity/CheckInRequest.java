package com.poolbooking.entity;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(@NotBlank String maVeQr) {
}
