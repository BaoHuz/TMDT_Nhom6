package com.poolbooking.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleRequest(
        @NotBlank String maDv,
        @NotBlank String maKhuVuc,
        @NotNull LocalDate ngay,
        @NotNull LocalTime gioBatDau,
        @NotNull LocalTime gioKetThuc,
        @Min(1) int sucChua) {
}
