package com.poolbooking.service;

import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.KhuVuc;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.DichVuRepository;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.KhuVucRepository;
import com.poolbooking.repository.LichDichVuRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    private final LichDichVuRepository scheduleRepository;
    private final DatLichRepository bookingRepository;
    private final DichVuRepository dichVuRepository;
    private final KhuVucRepository khuVucRepository;
    private final BookingService bookingService;

    public ScheduleService(LichDichVuRepository scheduleRepository, DatLichRepository bookingRepository,
            DichVuRepository dichVuRepository, KhuVucRepository khuVucRepository, BookingService bookingService) {
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.dichVuRepository = dichVuRepository;
        this.khuVucRepository = khuVucRepository;
        this.bookingService = bookingService;
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public List<Map<String, Object>> all(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date != null && date.isBefore(today)) {
            throw new BusinessException("Không thể xem lịch của ngày đã qua");
        }
        List<LichDichVu> schedules = date == null
                ? scheduleRepository.findByNgayBetweenAndTrangThaiInOrderByNgayAscGioBatDauAsc(
                        today, today.plusDays(30), List.of(TrangThaiLich.MO, TrangThaiLich.HET_CHO))
                : scheduleRepository.findByNgayAndTrangThaiInOrderByMaLichAsc(
                        date, List.of(TrangThaiLich.MO, TrangThaiLich.HET_CHO));
        LocalDateTime now = LocalDateTime.now();
        List<String> listedScheduleIds = schedules.stream().map(schedule -> schedule.maLich).toList();
        Set<String> expiredScheduleIds = listedScheduleIds.isEmpty()
                ? Set.of()
                : new HashSet<>(bookingRepository.findByMaLichInAndTrangThaiAndNgayDatBefore(
                                listedScheduleIds,
                                TrangThaiDatLich.CHO_THANH_TOAN,
                                now.minusMinutes(BookingService.HOLD_MINUTES))
                        .stream()
                        .map(booking -> booking.maLich)
                        .toList());
        Set<String> scheduleIdsToLock = new TreeSet<>();
        for (LichDichVu schedule : schedules) {
            if (expiredScheduleIds.contains(schedule.maLich)) {
                scheduleIdsToLock.add(schedule.maLich);
            }
        }
        for (String scheduleId : scheduleIdsToLock) {
            scheduleRepository.lockById(scheduleId).ifPresent(bookingService::expirePendingBookings);
        }
        List<LichDichVu> available = new ArrayList<>();
        for (LichDichVu listedSchedule : schedules) {
            if (LocalDateTime.of(listedSchedule.ngay, listedSchedule.gioBatDau).isAfter(now)
                    && listedSchedule.trangThai == TrangThaiLich.MO && listedSchedule.soChoCon > 0) {
                available.add(listedSchedule);
            }
        }
        return available.stream()
                .sorted(Comparator.comparing((LichDichVu schedule) -> schedule.ngay)
                        .thenComparing(schedule -> schedule.gioBatDau))
                .map(this::view)
                .toList();
    }

    public Map<String, Object> view(LichDichVu schedule) {
        DichVu service = dichVuRepository.findById(schedule.maDv).orElseThrow();
        KhuVuc area = khuVucRepository.findById(schedule.maKhuVuc).orElseThrow();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("maLich", schedule.maLich);
        map.put("ngay", schedule.ngay);
        map.put("gioBatDau", schedule.gioBatDau);
        map.put("gioKetThuc", schedule.gioKetThuc);
        map.put("soChoCon", schedule.soChoCon);
        map.put("sucChua", schedule.sucChua);
        map.put("trangThai", schedule.trangThai);
        map.put("maDv", service.maDv);
        map.put("maKhuVuc", area.maKhuVuc);
        map.put("dichVu", service.tenDv);
        map.put("donGia", service.donGia);
        map.put("khuVuc", area.tenKhuVuc);
        return map;
    }
}
