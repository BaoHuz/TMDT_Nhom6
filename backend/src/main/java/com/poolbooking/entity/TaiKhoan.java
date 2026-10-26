package com.poolbooking.entity;

import com.poolbooking.enums.VaiTro;
import jakarta.persistence.*;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {
    @Id @Column(name = "ma_tk") public String maTk;
    @Column(name = "ten_dang_nhap", nullable = false, unique = true) public String tenDangNhap;
    @Column(nullable = false, unique = true) public String email;
    @Column(name = "mat_khau_bam", nullable = false) public String matKhauBam;
    @Enumerated(EnumType.STRING) @Column(name = "vai_tro", nullable = false) public VaiTro vaiTro = VaiTro.CUSTOMER;
    @Column(name = "trang_thai", nullable = false) public String trangThai = "HOAT_DONG";
    @Column(name = "tao_luc", nullable = false, insertable = false, updatable = false) public java.time.LocalDateTime taoLuc;
}
