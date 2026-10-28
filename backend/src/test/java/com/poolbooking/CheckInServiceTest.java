package com.poolbooking;

import com.poolbooking.entity.*;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import com.poolbooking.service.CheckInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock private DatLichRepository bookings;
    @Mock private CheckInRepository checkIns;
    @Mock private TaiKhoanRepository users;
    @Mock private LichDichVuRepository schedules;

    private CheckInService checkInService;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInService(bookings, checkIns, users, schedules);
    }

    @Test
    void staffCheckIn_Success() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maLich = "L-01";
        booking.maVeQr = "QR-DL-01";
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-01";
        schedule.ngay = LocalDate.now();
        schedule.gioBatDau = LocalTime.now().minusMinutes(5);
        schedule.gioKetThuc = LocalTime.now().plusHours(1);

        TaiKhoan staff = new TaiKhoan();
        staff.maTk = "TK-STAFF";
        staff.tenDangNhap = "staff1";

        when(bookings.findByMaVeQr("QR-DL-01")).thenReturn(Optional.of(booking));
        when(schedules.lockById("L-01")).thenReturn(Optional.of(schedule));
        when(bookings.lockById("DL-01")).thenReturn(Optional.of(booking));
        when(checkIns.existsByMaDat("DL-01")).thenReturn(false);
        when(users.findByTenDangNhap("staff1")).thenReturn(Optional.of(staff));
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));

        DatLich checked = checkInService.check("QR-DL-01", "staff1");

        assertThat(checked.trangThai).isEqualTo(TrangThaiDatLich.DA_CHECK_IN);
        verify(checkIns).save(any(CheckIn.class));
    }

    @Test
    void staffCheckIn_AlreadyCheckedIn_ThrowsException() {
        DatLich booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maLich = "L-01";
        booking.maVeQr = "QR-DL-01";
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-01";
        when(bookings.findByMaVeQr("QR-DL-01")).thenReturn(Optional.of(booking));
        when(schedules.lockById("L-01")).thenReturn(Optional.of(schedule));
        when(bookings.lockById("DL-01")).thenReturn(Optional.of(booking));
        when(checkIns.existsByMaDat("DL-01")).thenReturn(true); // already checked in

        assertThatThrownBy(() -> checkInService.check("QR-DL-01", "staff1"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Đơn đã check-in");
    }
}
