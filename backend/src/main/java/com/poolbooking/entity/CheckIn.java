package com.poolbooking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in")
public class CheckIn {
    @Id @Column(name = "ma_ci") public String maCi;
    @Column(name = "ma_dat", nullable = false, unique = true) public String maDat;
    @Column(name = "ma_tk_nhan_vien", nullable = false) public String maTkNhanVien;
    @Column(name = "thoi_diem", nullable = false) public LocalDateTime thoiDiem = LocalDateTime.now();
    @Column(name = "ghi_chu") public String ghiChu;
}
