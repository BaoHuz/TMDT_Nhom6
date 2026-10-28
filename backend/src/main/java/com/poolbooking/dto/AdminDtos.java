package com.poolbooking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public final class AdminDtos {
    private AdminDtos() {}
    public record PoolRequest(@NotBlank String tenHoBoi,@NotBlank String diaChi,@NotNull LocalTime gioMoCua,@NotNull LocalTime gioDongCua,String trangThai) {}
    public record AreaRequest(@NotBlank String maHoBoi,@NotBlank String tenKhuVuc,@Min(1) int sucChua,String trangThai) {}
    public record ServiceRequest(@NotBlank String tenDv,@NotBlank String loaiDv,@DecimalMin("0.0") BigDecimal donGia,@Min(1) int thoiLuongPhut,String trangThai) {}
    public record ScheduleRequest(@NotBlank String maDv,@NotBlank String maKhuVuc,@NotNull LocalDate ngay,@NotNull LocalTime gioBatDau,@NotNull LocalTime gioKetThuc,@Min(1) int sucChua) {}
    public record StaffRequest(@NotBlank String username,@NotBlank @Email String email,@NotBlank @Size(min=8,max=72) String password,@NotBlank String fullName) {}
    public record CancelScheduleRequest(String reason) {}
    public record AccountResponse(String maTk,String username,String email,String role,String status,LocalDateTime createdAt) {}
    public record BookingResponse(String maDat,String maKh,String maLich,int soNguoi,BigDecimal donGiaTaiDat,
        BigDecimal tongTien,String trangThai,String trangThaiThanhToan,String trangThaiHoanTien,
        String maVeQr,LocalDateTime ngayDat,String lyDoHuy,LocalDateTime thoiGianHuy,
        LocalDateTime thoiDiemBatDauApDung,
        BigDecimal tyLeHoan,BigDecimal soTienHoan,LocalDateTime thoiGianGhiNhanHoan,
        String maYeuCauHoan,BigDecimal soTienYeuCauHoan,BigDecimal soTienHoanThucTe,
        LocalDateTime thoiGianHoanThucTe,String maGiaoDichHoan,
        String phuongThucThanhToan,String vnpayMaGiaoDichGoc,String ghiChuHoan,
        String vnpayThoiDiemGiaoDichGoc) {}
}
