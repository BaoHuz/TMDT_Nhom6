package com.poolbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in")
public class CheckIn {
    @Id
    @Column(name = "ma_ci")
    public String maCheckIn;
    @Column(unique = true)
    public String maDat;
    public String maTkNhanVien;
    public LocalDateTime thoiDiem = LocalDateTime.now();
    public String ghiChu;

    public CheckIn() {
    }
}
