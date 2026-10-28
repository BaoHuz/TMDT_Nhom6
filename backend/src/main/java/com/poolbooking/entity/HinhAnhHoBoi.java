package com.poolbooking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hinh_anh_ho_boi")
public class HinhAnhHoBoi {
    @Id @Column(name = "ma_anh") public String maAnh;
    @Column(name = "ma_ho_boi", nullable = false) public String maHoBoi;
    @Column(name = "duong_dan", nullable = false, length = 500) public String duongDan;
    @Column(name = "thu_tu", nullable = false) public int thuTu;
    @Column(name = "la_anh_dai_dien", nullable = false) public boolean laAnhDaiDien;
    @Column(name = "ngay_tao", nullable = false) public LocalDateTime ngayTao;

    @PrePersist
    void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
