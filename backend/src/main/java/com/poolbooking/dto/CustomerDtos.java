package com.poolbooking.dto;

import java.math.BigDecimal;
import java.time.*;

public final class CustomerDtos {
    private CustomerDtos() {}

    public record BookingSummaryResponse(String maDat, String maLich, String dichVu, String hoBoi,
        String khuVuc, LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc, int soNguoi,
        BigDecimal tongTien, String trangThai, String maVeQr) {}
    public record BookingDetailResponse(String maDat, String maLich, String dichVu, String hoBoi,
        String khuVuc, LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc, int soNguoi,
        BigDecimal donGiaTaiDat, BigDecimal tongTien, String trangThai, String trangThaiThanhToan,
        String trangThaiHoanTien, LocalDateTime thoiGianGhiNhanHoan, String maYeuCauHoan,
        BigDecimal soTienYeuCauHoan, BigDecimal soTienHoanThucTe,
        LocalDateTime thoiGianHoanThucTe, String maGiaoDichHoan,
        String maVeQr, String lyDoHuy, LocalDateTime thoiGianHuy,
        LocalDateTime thoiDiemBatDauApDung, BigDecimal tyLeHoan,
        BigDecimal soTienHoan, LocalDateTime hanThanhToan) {}
    public record CancellationPreviewResponse(boolean eligible, long hoursRemaining,
        BigDecimal refundRate, BigDecimal refundAmount, String message) {}
    public record CancelRequest(String reason) {}
    public record ProfileResponse(String username, String fullName, String email,
                                  String phone, String status) {}
    public record ProfileUpdateRequest(String fullName, String phone) {}
}
