package com.poolbooking.repository;

import com.poolbooking.entity.LichDichVu;
import com.poolbooking.enums.TrangThaiLich;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface LichDichVuRepository extends JpaRepository<LichDichVu, String> {
    @Query("""
        select l from LichDichVu l
        where l.ngay = :ngay and l.trangThai = :trangThai
          and (:maKhuVuc is null or l.maKhuVuc = :maKhuVuc)
          and (:maDv is null or l.maDv = :maDv)
        order by l.gioBatDau
        """)
    List<LichDichVu> search(@Param("ngay") LocalDate ngay,
                            @Param("trangThai") TrangThaiLich trangThai,
                            @Param("maKhuVuc") String maKhuVuc,
                            @Param("maDv") String maDv);

    List<LichDichVu> findByNgayAndTrangThai(LocalDate ngay, TrangThaiLich trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LichDichVu l where l.maLich = :id")
    Optional<LichDichVu> lockById(@Param("id") String id);
}
