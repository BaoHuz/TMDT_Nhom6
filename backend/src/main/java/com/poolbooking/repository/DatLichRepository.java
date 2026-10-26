package com.poolbooking.repository;

import com.poolbooking.entity.DatLich;
import com.poolbooking.enums.TrangThaiDatLich;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DatLichRepository extends JpaRepository<DatLich, String> {
    List<DatLich> findByMaKhOrderByNgayDatDesc(String maKh);
    List<DatLich> findByMaLich(String maLich);
    Optional<DatLich> findByMaVeQr(String maVeQr);
    long countByTrangThai(TrangThaiDatLich trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DatLich d where d.maDat = :id")
    Optional<DatLich> lockById(@Param("id") String id);
}
