package com.poolbooking.service;

import com.poolbooking.dto.CatalogDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.ResourceNotFoundException;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class CatalogService {
    private final HoBoiRepository pools; private final KhuVucRepository areas;
    private final DichVuRepository services; private final LichDichVuRepository schedules;
    private final PoolImageService poolImages;
    public CatalogService(HoBoiRepository pools, KhuVucRepository areas,
                          DichVuRepository services, LichDichVuRepository schedules,
                          PoolImageService poolImages) {
        this.pools = pools; this.areas = areas; this.services = services; this.schedules = schedules;
        this.poolImages = poolImages;
    }

    @Transactional(readOnly = true)
    public List<PoolSummaryResponse> pools() { return pools.findAll().stream().map(this::pool).toList(); }

    @Transactional(readOnly = true)
    public PoolDetailResponse pool(String id) {
        HoBoi value = pools.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ bơi"));
        List<AreaResponse> areaViews = areas.findByMaHoBoi(id).stream().map(this::area).toList();
        Set<String> areaIds = new HashSet<>(areaViews.stream().map(AreaResponse::maKhuVuc).toList());
        List<ScheduleResponse> upcoming = schedules.findAll().stream()
            .filter(x -> areaIds.contains(x.maKhuVuc) && !x.ngay.isBefore(LocalDate.now())
                && (x.trangThai == TrangThaiLich.MO || x.trangThai == TrangThaiLich.HET_CHO))
            .sorted(Comparator.comparing((LichDichVu x) -> x.ngay).thenComparing(x -> x.gioBatDau))
            .limit(12).map(this::schedule).toList();
        return new PoolDetailResponse(pool(value), areaViews, upcoming, poolImages.list(id));
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> areas(String poolId) {
        List<KhuVuc> values = poolId == null || poolId.isBlank() ? areas.findAll() : areas.findByMaHoBoi(poolId);
        return values.stream().map(this::area).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> services() {
        return services.findByTrangThai("DANG_BAN").stream().map(this::service).toList();
    }

    PoolSummaryResponse pool(HoBoi x) { return new PoolSummaryResponse(x.maHoBoi, x.tenHoBoi, x.diaChi, x.gioMoCua, x.gioDongCua, x.trangThai); }
    AreaResponse area(KhuVuc x) { return new AreaResponse(x.maKhuVuc, x.maHoBoi, x.tenKhuVuc, x.sucChua, x.trangThai); }
    ServiceResponse service(DichVu x) { return new ServiceResponse(x.maDv, x.tenDv, x.loaiDv, x.donGia, x.thoiLuongPhut, x.trangThai); }
    public ScheduleResponse schedule(LichDichVu x) {
        DichVu service = services.findById(x.maDv).orElseThrow();
        KhuVuc area = areas.findById(x.maKhuVuc).orElseThrow();
        HoBoi pool = pools.findById(area.maHoBoi).orElseThrow();
        return new ScheduleResponse(x.maLich, service.maDv, service.tenDv, service.donGia,
            area.maKhuVuc, area.tenKhuVuc, pool.maHoBoi, pool.tenHoBoi, x.ngay,
            x.gioBatDau, x.gioKetThuc, x.sucChua, x.soChoCon, x.trangThai.name());
    }
}
