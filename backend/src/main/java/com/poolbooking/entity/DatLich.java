package com.poolbooking.entity;

import com.poolbooking.enums.TrangThaiDatLich;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dat_lich")
public class DatLich {
    @Id
    public String maDat;
    public String maKh;
    public String maLich;
    public String maVeQr;
    public int soNguoi;
    public BigDecimal donGiaTaiDat;
    public BigDecimal tongTien;
    public LocalDateTime ngayDat = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    public TrangThaiDatLich trangThai = TrangThaiDatLich.CHO_THANH_TOAN;

    public DatLich() {
    }
}
