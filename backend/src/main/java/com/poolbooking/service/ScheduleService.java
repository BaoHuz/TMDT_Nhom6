package com.poolbooking.service;

import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.ResourceNotFoundException;
import com.poolbooking.repository.LichDichVuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class ScheduleService {
    private final LichDichVuRepository schedules; private final CatalogService catalog;
    public ScheduleService(LichDichVuRepository schedules, CatalogService catalog) {
        this.schedules = schedules; this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> search(LocalDate date, String maHoBoi, String maKhuVuc,
                                         String maDv, String sort) {
        Comparator<ScheduleResponse> comparator = switch (sort == null ? "earliest" : sort) {
            case "latest" -> Comparator.comparing(ScheduleResponse::ngay).thenComparing(ScheduleResponse::gioBatDau).reversed();
            case "priceAsc" -> Comparator.comparing(ScheduleResponse::donGia);
            case "priceDesc" -> Comparator.comparing(ScheduleResponse::donGia).reversed();
            case "available" -> Comparator.comparingInt(ScheduleResponse::soChoCon).reversed();
            default -> Comparator.comparing(ScheduleResponse::ngay).thenComparing(ScheduleResponse::gioBatDau);
        };
        LocalDate from = date == null ? LocalDate.now() : date;
        return schedules.findAll().stream()
            .filter(x -> x.trangThai == TrangThaiLich.MO)
            .filter(x -> date == null ? !x.ngay.isBefore(from) : x.ngay.equals(date))
            .map(catalog::schedule)
            .filter(x -> blank(maHoBoi) || x.maHoBoi().equals(maHoBoi))
            .filter(x -> blank(maKhuVuc) || x.maKhuVuc().equals(maKhuVuc))
            .filter(x -> blank(maDv) || x.maDv().equals(maDv))
            .sorted(comparator).toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse get(String id) {
        return catalog.schedule(schedules.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi")));
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
