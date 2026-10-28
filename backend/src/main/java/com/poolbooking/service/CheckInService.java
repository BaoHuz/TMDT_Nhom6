package com.poolbooking.service;

import com.poolbooking.entity.*;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckInService {
    private final DatLichRepository bookings; private final CheckInRepository checkIns;
    private final TaiKhoanRepository users; private final LichDichVuRepository schedules;
    public CheckInService(DatLichRepository bookings, CheckInRepository checkIns,
                          TaiKhoanRepository users, LichDichVuRepository schedules) {
        this.bookings = bookings; this.checkIns = checkIns; this.users = users; this.schedules = schedules;
    }

    @Transactional
    public DatLich check(String qr, String staffUsername) {
        DatLich existing = bookings.findByMaVeQr(qr)
            .orElseThrow(() -> new BusinessException("Mã QR không hợp lệ"));
        LichDichVu schedule = schedules.lockById(existing.maLich)
            .orElseThrow(() -> new BusinessException("Không tìm thấy ca bơi"));
        DatLich booking = bookings.lockById(existing.maDat)
            .orElseThrow(() -> new BusinessException("Mã QR không hợp lệ"));
        if (!qr.equals(booking.maVeQr)) throw new BusinessException("Mã QR không hợp lệ");
        if (booking.trangThai != TrangThaiDatLich.DA_XAC_NHAN)
            throw new BusinessException("Đơn chưa đủ điều kiện check-in");
        if (checkIns.existsByMaDat(booking.maDat)) throw new BusinessException("Đơn đã check-in");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime opens = LocalDateTime.of(schedule.ngay, schedule.gioBatDau).minusMinutes(15);
        LocalDateTime closes = LocalDateTime.of(schedule.ngay, schedule.gioKetThuc);
        if (now.isBefore(opens) || now.isAfter(closes))
            throw new BusinessException("Chưa đến hoặc đã quá thời gian check-in");
        TaiKhoan staff = users.findByTenDangNhap(staffUsername).orElseThrow();
        CheckIn checkIn = new CheckIn(); checkIn.maCi = "CI-" + UUID.randomUUID();
        checkIn.maDat = booking.maDat; checkIn.maTkNhanVien = staff.maTk;
        checkIns.save(checkIn); booking.trangThai = TrangThaiDatLich.DA_CHECK_IN;
        return bookings.save(booking);
    }

    @Transactional(readOnly = true)
    public DatLich preview(String qr) {
        DatLich booking = bookings.findByMaVeQr(qr)
            .orElseThrow(() -> new BusinessException("Mã QR không hợp lệ"));
        if (booking.trangThai != TrangThaiDatLich.DA_XAC_NHAN)
            throw new BusinessException("Đơn chưa đủ điều kiện check-in");
        if (checkIns.existsByMaDat(booking.maDat)) throw new BusinessException("Đơn đã check-in");
        return booking;
    }
}
