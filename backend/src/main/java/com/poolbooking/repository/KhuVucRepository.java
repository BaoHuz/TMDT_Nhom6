package com.poolbooking.repository;

import com.poolbooking.entity.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KhuVucRepository extends JpaRepository<KhuVuc, String> {
    List<KhuVuc> findByMaHoBoi(String maHoBoi);
}
