package com.poolbooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    public String maKh;
    public String maTk;
    public String hoTen;
    public String dienThoai;
    public String email;
    public String trangThai = "HOAT_DONG";

    public KhachHang() {
    }
}
