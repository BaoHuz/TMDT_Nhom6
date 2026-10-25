package com.poolbooking.repository;

import com.poolbooking.entity.DatLich;
import com.poolbooking.enums.TrangThaiDatLich;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatLichRepository extends JpaRepository<DatLich, String> {
    List<DatLich> findByMaKhOrderByNgayDatDesc(String id);

    List<DatLich> findByMaLichAndTrangThaiOrderByNgayDatAsc(String scheduleId, TrangThaiDatLich status);

    List<DatLich> findByTrangThaiAndNgayDatBefore(TrangThaiDatLich status, LocalDateTime cutoff);

    List<DatLich> findByMaLichInAndTrangThaiAndNgayDatBefore(
            Collection<String> scheduleIds, TrangThaiDatLich status, LocalDateTime cutoff);

    Optional<DatLich> findByMaVeQr(String qr);

    long countByTrangThai(TrangThaiDatLich status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DatLich d where d.maDat = :id")
    Optional<DatLich> lockById(@Param("id") String id);
}
