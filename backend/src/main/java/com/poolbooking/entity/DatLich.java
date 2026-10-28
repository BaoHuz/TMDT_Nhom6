package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiDatLich;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dat_lich")
public class DatLich {
    @Id @Column(name = "ma_dat") public String maDat;
    @Column(name = "ma_kh", nullable = false) public String maKh;
    @Column(name = "ma_lich", nullable = false) public String maLich;
    @Column(name = "so_nguoi", nullable = false) public int soNguoi;
    @Column(name = "don_gia_tai_dat", nullable = false, precision = 18, scale = 2) public BigDecimal donGiaTaiDat;
    @Column(name = "tong_tien", nullable = false, precision = 18, scale = 2) public BigDecimal tongTien;
    @Column(name = "ma_ve_qr", unique = true) public String maVeQr;
    @Column(name = "ngay_dat", nullable = false) public LocalDateTime ngayDat = LocalDateTime.now();
    @Column(name = "han_thanh_toan", nullable = false) public LocalDateTime hanThanhToan;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false) public TrangThaiDatLich trangThai;
    @Column(name = "ly_do_huy") public String lyDoHuy;
    @Column(name = "thoi_gian_huy") public LocalDateTime thoiGianHuy;
    @Column(name = "ty_le_hoan", precision = 5, scale = 2) public BigDecimal tyLeHoan;
    @Column(name = "so_tien_hoan", precision = 18, scale = 2) public BigDecimal soTienHoan;
}
