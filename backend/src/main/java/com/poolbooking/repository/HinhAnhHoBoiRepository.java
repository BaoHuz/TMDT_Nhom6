package com.poolbooking.repository;

import com.poolbooking.entity.HinhAnhHoBoi;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HinhAnhHoBoiRepository extends JpaRepository<HinhAnhHoBoi, String> {
    List<HinhAnhHoBoi> findByMaHoBoiOrderByLaAnhDaiDienDescThuTuAscNgayTaoAsc(String maHoBoi);
    boolean existsByMaHoBoiAndLaAnhDaiDienTrue(String maHoBoi);

    @Modifying
    @Query("update HinhAnhHoBoi h set h.laAnhDaiDien = false where h.maHoBoi = :maHoBoi")
    void clearRepresentative(@Param("maHoBoi") String maHoBoi);
}
