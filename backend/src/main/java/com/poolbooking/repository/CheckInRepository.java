package com.poolbooking.repository;

import com.poolbooking.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, String> {
    boolean existsByMaDat(String id);

    long count();
}
