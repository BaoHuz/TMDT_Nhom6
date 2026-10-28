package com.poolbooking.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String fullName,
        String phone
    ) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record TokenData(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        String role,
        String fullName
    ) {}
}
