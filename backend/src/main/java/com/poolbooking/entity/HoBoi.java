package com.poolbooking.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "ho_boi")
public class HoBoi {
    @Id @Column(name = "ma_ho_boi") public String maHoBoi;
    @Column(name = "ten_ho_boi", nullable = false) public String tenHoBoi;
    @Column(name = "dia_chi", nullable = false) public String diaChi;
    @Column(name = "gio_mo_cua", nullable = false) public LocalTime gioMoCua;
    @Column(name = "gio_dong_cua", nullable = false) public LocalTime gioDongCua;
    @Column(name = "trang_thai", nullable = false) public String trangThai = "HOAT_DONG";
}
