package com.poolbooking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id @Column(name = "ma_kh") public String maKh;
    @Column(name = "ma_tk", unique = true) public String maTk;
    @Column(name = "ho_ten", nullable = false) public String hoTen;
    @Column(name = "dien_thoai") public String dienThoai;
    public String email;
    @Column(name = "trang_thai", nullable = false) public String trangThai = "HOAT_DONG";
}
