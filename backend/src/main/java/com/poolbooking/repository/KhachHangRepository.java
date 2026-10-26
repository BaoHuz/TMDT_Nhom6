package com.poolbooking.repository;

import com.poolbooking.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    Optional<KhachHang> findByMaTk(String maTk);
}
