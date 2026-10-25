package com.poolbooking.repository;

import com.poolbooking.entity.LichDichVu;
import com.poolbooking.enums.TrangThaiLich;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

public interface LichDichVuRepository extends JpaRepository<LichDichVu, String> {
    List<LichDichVu> findByNgayAndTrangThai(LocalDate date, TrangThaiLich status);

    List<LichDichVu> findByNgayAndTrangThaiInOrderByMaLichAsc(
            LocalDate date, Collection<TrangThaiLich> statuses);

    List<LichDichVu> findByNgayBetweenAndTrangThaiInOrderByNgayAscGioBatDauAsc(
            LocalDate startDate, LocalDate endDate, Collection<TrangThaiLich> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select l from LichDichVu l where l.maLich = :id")
    Optional<LichDichVu> lockById(@Param("id") String id);
}
