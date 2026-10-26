package com.poolbooking.repository;

import com.poolbooking.entity.DichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DichVuRepository extends JpaRepository<DichVu, String> {
    List<DichVu> findByTrangThai(String trangThai);
}
