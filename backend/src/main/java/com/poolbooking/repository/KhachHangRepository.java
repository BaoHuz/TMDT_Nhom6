package com.poolbooking.repository;

import com.poolbooking.entity.KhachHang;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    Optional<KhachHang> findByMaTk(String maTk);
}
