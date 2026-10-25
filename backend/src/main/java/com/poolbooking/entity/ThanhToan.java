package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiThanhToan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @Column(name = "ma_tt")
    public String maThanhToan;
    public String maDat;
    public String phuongThuc;
    public String maGiaoDich;
    public BigDecimal soTien;
    public LocalDateTime thoiDiem = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    public TrangThaiThanhToan trangThai;

    public ThanhToan() {
    }
}
