package com.poolbooking.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String tenDangNhap,
        @NotBlank @Size(min = 6) String matKhau,
        @NotBlank String hoTen,
        @Email String email,
        String dienThoai) {
}
