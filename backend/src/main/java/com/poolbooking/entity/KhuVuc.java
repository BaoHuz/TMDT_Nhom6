package com.poolbooking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "khu_vuc")
public class KhuVuc {
    @Id @Column(name = "ma_khu_vuc") public String maKhuVuc;
    @Column(name = "ma_ho_boi", nullable = false) public String maHoBoi;
    @Column(name = "ten_khu_vuc", nullable = false) public String tenKhuVuc;
    @Column(name = "suc_chua", nullable = false) public int sucChua;
    @Column(name = "trang_thai", nullable = false) public String trangThai = "HOAT_DONG";
}
