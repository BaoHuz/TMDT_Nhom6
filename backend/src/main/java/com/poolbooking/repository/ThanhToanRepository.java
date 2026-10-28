package com.poolbooking.repository;

import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    boolean existsByMaGiaoDich(String maGiaoDich);
    List<ThanhToan> findByTrangThai(TrangThaiThanhToan trangThai);
    Optional<ThanhToan> findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(String maDat, TrangThaiThanhToan trangThai);
    Optional<ThanhToan> findFirstByMaDatOrderByThoiDiemDesc(String maDat);
    Optional<ThanhToan> findByMaGiaoDich(String maGiaoDich);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ThanhToan p where p.maTt = :id")
    Optional<ThanhToan> lockById(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p from ThanhToan p
        where p.maDat = :maDat and p.trangThai = :trangThai
        order by p.thoiDiem desc
        """)
    List<ThanhToan> lockSuccessfulPayments(
        @Param("maDat") String maDat,
        @Param("trangThai") TrangThaiThanhToan trangThai
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ThanhToan p where p.maGiaoDich = :maGiaoDich")
    Optional<ThanhToan> lockByMaGiaoDich(@Param("maGiaoDich") String maGiaoDich);
}
