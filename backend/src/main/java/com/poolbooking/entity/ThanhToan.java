package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiThanhToan;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id @Column(name = "ma_tt") public String maTt;
    @Column(name = "ma_dat", nullable = false) public String maDat;
    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2) public BigDecimal soTien;
    @Column(name = "phuong_thuc", nullable = false) public String phuongThuc;
    @Column(name = "ma_giao_dich", unique = true) public String maGiaoDich;
    @Column(name = "thoi_diem", nullable = false) public LocalDateTime thoiDiem = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false) public TrangThaiThanhToan trangThai;
    @Column(name = "trang_thai_hoan") public String trangThaiHoan;
    @Column(name = "so_tien_hoan", precision = 18, scale = 2) public BigDecimal soTienHoan;
    @Column(name = "thoi_gian_hoan") public LocalDateTime thoiGianHoan;
    @Column(name = "ma_giao_dich_hoan") public String maGiaoDichHoan;
}
