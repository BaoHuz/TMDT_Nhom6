package com.poolbooking.repository;

import com.poolbooking.entity.DatLich;
import com.poolbooking.enums.TrangThaiDatLich;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
import java.time.LocalDateTime;

public interface DatLichRepository extends JpaRepository<DatLich, String> {
    List<DatLich> findByMaKhOrderByNgayDatDesc(String maKh);
    List<DatLich> findByMaLich(String maLich);
    Optional<DatLich> findByMaVeQr(String maVeQr);
    long countByTrangThai(TrangThaiDatLich trangThai);
    List<DatLich> findByTrangThaiAndHanThanhToanLessThanEqual(TrangThaiDatLich trangThai, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DatLich d where d.maDat = :id")
    Optional<DatLich> lockById(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DatLich d where d.maLich = :maLich order by d.maDat")
    List<DatLich> lockByMaLich(@Param("maLich") String maLich);
}
