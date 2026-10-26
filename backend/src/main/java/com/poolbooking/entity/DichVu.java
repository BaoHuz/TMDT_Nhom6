package com.poolbooking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dich_vu")
public class DichVu {
    @Id @Column(name = "ma_dv") public String maDv;
    @Column(name = "ten_dv", nullable = false) public String tenDv;
    @Column(name = "loai_dv", nullable = false) public String loaiDv;
    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2) public BigDecimal donGia;
    @Column(name = "thoi_luong_phut", nullable = false) public int thoiLuongPhut;
    @Column(name = "trang_thai", nullable = false) public String trangThai = "DANG_BAN";
}
