package com.poolbooking.dto;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record PoolSummaryResponse(String maHoBoi, String tenHoBoi, String diaChi,
                                      LocalTime gioMoCua, LocalTime gioDongCua, String trangThai) {}
    public record AreaResponse(String maKhuVuc, String maHoBoi, String tenKhuVuc,
                               int sucChua, String trangThai) {}
    public record ServiceResponse(String maDv, String tenDv, String loaiDv, BigDecimal donGia,
                                  int thoiLuongPhut, String trangThai) {}
    public record ScheduleResponse(String maLich, String maDv, String dichVu, BigDecimal donGia,
                                   String maKhuVuc, String khuVuc, String maHoBoi, String hoBoi,
                                   LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc,
                                   int sucChua, int soChoCon, String trangThai) {}
    public record PoolDetailResponse(PoolSummaryResponse pool, List<AreaResponse> areas,
                                     List<ScheduleResponse> upcomingSchedules) {}
}
