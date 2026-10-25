package com.poolbooking.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ServiceRequest(@NotBlank String tenDv, @Min(0) BigDecimal donGia, @Min(1) int thoiLuongPhut) {
}
