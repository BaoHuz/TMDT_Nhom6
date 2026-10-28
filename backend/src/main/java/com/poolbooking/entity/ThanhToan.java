package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiThanhToan;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
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
    @Column(name = "vnpay_ma_giao_dich_goc", length = 100) public String vnpayMaGiaoDichGoc;
    @Column(name = "vnpay_thoi_diem_giao_dich_goc", length = 14) public String vnpayThoiDiemGiaoDichGoc;
    @Column(name = "thoi_diem", nullable = false) public LocalDateTime thoiDiem = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false) public TrangThaiThanhToan trangThai;
    @Column(name = "trang_thai_hoan") public String trangThaiHoan;
    @Column(name = "so_tien_yeu_cau_hoan", precision = 18, scale = 2) public BigDecimal soTienYeuCauHoan;
    @Column(name = "thoi_gian_yeu_cau_hoan") public LocalDateTime thoiGianYeuCauHoan;
    @Column(name = "ma_yeu_cau_hoan", length = 60) public String maYeuCauHoan;
    @Column(name = "so_tien_hoan", precision = 18, scale = 2) public BigDecimal soTienHoan;
    @Column(name = "thoi_gian_hoan") public LocalDateTime thoiGianHoan;
    @Column(name = "ma_giao_dich_hoan") public String maGiaoDichHoan;
    @Nationalized @Column(name = "ghi_chu_hoan", length = 500) public String ghiChuHoan;
}
