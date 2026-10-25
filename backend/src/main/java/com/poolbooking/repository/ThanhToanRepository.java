package com.poolbooking.repository;

import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiThanhToan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    boolean existsByMaGiaoDich(String value);

    List<ThanhToan> findByTrangThai(TrangThaiThanhToan status);
}
