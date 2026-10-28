package com.poolbooking;

import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiThanhToan;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.repository.*;
import com.poolbooking.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock private DatLichRepository bookings;
    @Mock private ThanhToanRepository payments;
    @Mock private CheckInRepository checkIns;
    @Mock private LichDichVuRepository schedules;
    @Mock private DichVuRepository services;
    @Mock private KhuVucRepository areas;
    @Mock private HoBoiRepository pools;
    @Mock private TaiKhoanRepository users;
    @Mock private KhachHangRepository customers;
    @Mock private PasswordEncoder encoder;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(bookings, payments, checkIns, schedules, services, areas, pools, users, customers, encoder);
    }

    @Test
    void cancelScheduleRecordsRefundRequestWithoutClaimingRefundWasPaid() {
        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-01";
        schedule.ngay = LocalDate.now().plusDays(1);
        schedule.gioBatDau = LocalTime.of(10, 0);
        schedule.trangThai = TrangThaiLich.MO;

        DatLich booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maLich = schedule.maLich;
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        ThanhToan payment = new ThanhToan();
        payment.maDat = booking.maDat;
        payment.soTien = booking.tongTien;
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;

        when(schedules.lockById(schedule.maLich)).thenReturn(Optional.of(schedule));
        when(bookings.lockByMaLich(schedule.maLich)).thenReturn(List.of(booking));
        when(checkIns.existsByMaDat(booking.maDat)).thenReturn(false);
        when(payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(
            booking.maDat, TrangThaiThanhToan.THANH_CONG
        )).thenReturn(Optional.of(payment));

        service.cancelSchedule(schedule.maLich, "Sự cố kỹ thuật");

        assertThat(booking.trangThai).isEqualTo(TrangThaiDatLich.DA_HUY);
        assertThat(booking.tyLeHoan).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(booking.thoiDiemBatDauApDung).isEqualTo(
            schedule.ngay.atTime(schedule.gioBatDau)
        );
        assertThat(payment.trangThaiHoan).isEqualTo("CHO_HOAN");
        assertThat(payment.soTienYeuCauHoan).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(payment.maYeuCauHoan).startsWith("REF-REQ-");
        assertThat(payment.soTienHoan).isNull();
        assertThat(payment.thoiGianHoan).isNull();
        assertThat(payment.maGiaoDichHoan).isNull();
        verify(bookings).save(booking);
        verify(payments).save(payment);
    }

    @Test
    void dashboardSubtractsPendingAndCompletedRefundsSeparately() {
        ThanhToan pending = new ThanhToan();
        pending.soTien = BigDecimal.valueOf(100000);
        pending.trangThaiHoan = "CHO_HOAN";
        pending.soTienYeuCauHoan = BigDecimal.valueOf(100000);

        ThanhToan refunded = new ThanhToan();
        refunded.soTien = BigDecimal.valueOf(80000);
        refunded.trangThaiHoan = "HOAN_MOT_PHAN";
        refunded.soTienHoan = BigDecimal.valueOf(20000);

        ThanhToan processing = new ThanhToan();
        processing.soTien = BigDecimal.valueOf(30000);
        processing.trangThaiHoan = "DANG_XU_LY";
        processing.soTienYeuCauHoan = BigDecimal.valueOf(10000);

        when(payments.findByTrangThai(TrangThaiThanhToan.THANH_CONG))
            .thenReturn(List.of(pending, processing, refunded));
        when(schedules.findByNgayAndTrangThai(LocalDate.now(), TrangThaiLich.MO)).thenReturn(List.of());

        Map<String, Object> dashboard = service.dashboard();

        assertThat(dashboard.get("doanhThu")).isEqualTo(BigDecimal.valueOf(80000));
        assertThat(dashboard.get("tienHoanChoXuLy")).isEqualTo(BigDecimal.valueOf(110000));
        assertThat(dashboard.get("tienDaHoanThucTe")).isEqualTo(BigDecimal.valueOf(20000));
    }
}
