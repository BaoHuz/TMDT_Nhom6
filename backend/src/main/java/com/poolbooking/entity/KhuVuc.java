package com.poolbooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "khu_vuc")
public class KhuVuc {
    @Id
    public String maKhuVuc;
    public String maHoBoi;
    public String tenKhuVuc;
    public int sucChua;
    public String trangThai = "HOAT_DONG";

    public KhuVuc() {
    }
}
