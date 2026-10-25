package com.poolbooking.entity;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String tenDangNhap, @NotBlank String matKhau) {
}
