package com.poolbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "dich_vu")
public class DichVu {
    @Id
    public String maDv;
    public String tenDv;
    public String loaiDv = "VE_BOI";
    @Column(precision = 18, scale = 2)
    public BigDecimal donGia;
    public int thoiLuongPhut;
    public String trangThai = "DANG_BAN";

    public DichVu() {
    }
}
