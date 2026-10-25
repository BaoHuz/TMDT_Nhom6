package com.poolbooking.service;

import com.poolbooking.entity.CheckIn;
import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.CheckInRepository;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CheckInService {
    private final DatLichRepository bookingRepository;
    private final LichDichVuRepository scheduleRepository;
    private final CheckInRepository checkInRepository;
    private final TaiKhoanRepository userRepository;

    public CheckInService(DatLichRepository bookingRepository, LichDichVuRepository scheduleRepository,
            CheckInRepository checkInRepository, TaiKhoanRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DatLich check(String qrCode, String staffUserName) {
        DatLich initial = bookingRepository.findByMaVeQr(qrCode)
                .orElseThrow(() -> new BusinessException("Mã QR không hợp lệ"));
        LichDichVu schedule = scheduleRepository.lockById(initial.maLich)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca bơi"));
        DatLich booking = bookingRepository.lockById(initial.maDat)
                .orElseThrow(() -> new BusinessException("Mã QR không hợp lệ"));
        if (booking.trangThai != TrangThaiDatLich.DA_XAC_NHAN) {
            throw new BusinessException("Đơn chưa đủ điều kiện check-in");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(schedule.ngay, schedule.gioBatDau);
        LocalDateTime end = LocalDateTime.of(schedule.ngay, schedule.gioKetThuc);
        if (now.isBefore(start) || now.isAfter(end)) {
            throw new BusinessException("Vé chỉ dùng được trong khung giờ của ca bơi");
        }
        if (checkInRepository.existsByMaDat(booking.maDat)) {
            throw new BusinessException("Đơn đã check-in");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.maCheckIn = "CI-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        checkIn.maDat = booking.maDat;
        TaiKhoan staff = userRepository.findByTenDangNhap(staffUserName).orElseThrow();
        checkIn.maTkNhanVien = staff.maTk;
        checkInRepository.save(checkIn);

        booking.trangThai = TrangThaiDatLich.DA_CHECK_IN;
        return bookingRepository.save(booking);
    }
}
