package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiDatLich;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingView(
        String maDat,
        String maLich,
        String maVeQr,
        int soNguoi,
        BigDecimal donGiaTaiDat,
        BigDecimal tongTien,
        LocalDateTime ngayDat,
        LocalDateTime holdExpiresAt,
        LocalDateTime validUntil,
        TrangThaiDatLich trangThai,
        String dichVu,
        String khuVuc,
        LocalDate ngay,
        LocalTime gioBatDau,
        LocalTime gioKetThuc) {
}
