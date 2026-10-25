package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiLich;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lich_dich_vu")
public class LichDichVu {
    @Id
    public String maLich;
    public String maDv;
    public String maKhuVuc;
    public LocalDate ngay;
    public LocalTime gioBatDau;
    public LocalTime gioKetThuc;
    public int sucChua;
    public int soChoCon;
    @Enumerated(EnumType.STRING)
    public TrangThaiLich trangThai = TrangThaiLich.MO;

    public LichDichVu() {
    }
}
