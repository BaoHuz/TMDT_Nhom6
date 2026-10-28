package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiLich;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lich_dich_vu")
public class LichDichVu {
    @Id @Column(name = "ma_lich") public String maLich;
    @Column(name = "ma_dv", nullable = false) public String maDv;
    @Column(name = "ma_khu_vuc", nullable = false) public String maKhuVuc;
    @Column(nullable = false) public LocalDate ngay;
    @Column(name = "gio_bat_dau", nullable = false) public LocalTime gioBatDau;
    @Column(name = "gio_ket_thuc", nullable = false) public LocalTime gioKetThuc;
    @Column(name = "suc_chua", nullable = false) public int sucChua;
    @Column(name = "so_cho_con", nullable = false) public int soChoCon;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false) public TrangThaiLich trangThai;
    @Version @Column(name = "row_version", nullable = false) public Long version;
}
