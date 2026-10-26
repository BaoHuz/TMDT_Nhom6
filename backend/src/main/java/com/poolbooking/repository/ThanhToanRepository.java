package com.poolbooking.repository;

import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    boolean existsByMaGiaoDich(String maGiaoDich);
    List<ThanhToan> findByTrangThai(TrangThaiThanhToan trangThai);
    Optional<ThanhToan> findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(String maDat, TrangThaiThanhToan trangThai);
    Optional<ThanhToan> findFirstByMaDatOrderByThoiDiemDesc(String maDat);
}
