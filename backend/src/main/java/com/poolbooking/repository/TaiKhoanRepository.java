package com.poolbooking.repository;

import com.poolbooking.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmailIgnoreCase(String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmailIgnoreCase(String email);
}
