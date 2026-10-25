package com.poolbooking.entity;

import com.poolbooking.enums.VaiTro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {
    @Id
    public String maTk;
    @Column(unique = true, nullable = false)
    public String tenDangNhap;
    @Column(nullable = false)
    public String matKhauBam;
    @Enumerated(EnumType.STRING)
    public VaiTro vaiTro = VaiTro.CUSTOMER;
    public String trangThai = "HOAT_DONG";

    public TaiKhoan() {
    }
}
