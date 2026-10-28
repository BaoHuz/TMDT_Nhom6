package com.poolbooking;

import com.poolbooking.dto.BookingDtos.BookingRequest;
import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import com.poolbooking.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private DatLichRepository bookings;
    @Mock private LichDichVuRepository schedules;
    @Mock private DichVuRepository services;
    @Mock private TaiKhoanRepository users;
    @Mock private KhachHangRepository customers;
    @Mock private ThanhToanRepository payments;
    @Mock private CheckInRepository checkIns;
    @Mock private CatalogService catalog;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
            bookings, schedules, services, users, customers, payments, checkIns, catalog
        );
    }

    @Test
    void customerBooking_Success() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";

        KhachHang customer = new KhachHang();
        customer.maKh = "KH-1";
        customer.maTk = "TK-1";

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-001";
        schedule.maDv = "DV-01";
        schedule.ngay = LocalDate.now().plusDays(1);
        schedule.gioBatDau = LocalTime.of(8, 0);
        schedule.gioKetThuc = LocalTime.of(9, 30);
        schedule.sucChua = 30;
        schedule.soChoCon = 10;
        schedule.trangThai = TrangThaiLich.MO;

        DichVu service = new DichVu();
        service.maDv = "DV-01";
        service.donGia = BigDecimal.valueOf(50000);

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(customer));
        when(schedules.lockById("L-001")).thenReturn(Optional.of(schedule));
        when(services.findById("DV-01")).thenReturn(Optional.of(service));
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schedules.findById("L-001")).thenReturn(Optional.of(schedule));
        when(catalog.schedule(any(LichDichVu.class))).thenReturn(new ScheduleResponse(
            "L-001", "DV-01", "Vé người lớn", BigDecimal.valueOf(50000),
            "KV-01", "Khu người lớn", "HB-01", "Hồ Thảo Điền",
            schedule.ngay, schedule.gioBatDau, schedule.gioKetThuc, 30, 8, "MO"
        ));

        BookingDetailResponse res = bookingService.create("customer1", new BookingRequest("L-001", 2));

        assertThat(res).isNotNull();
        assertThat(res.soNguoi()).isEqualTo(2);
        assertThat(res.donGiaTaiDat()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(res.tongTien()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(res.trangThai()).isEqualTo("CHO_THANH_TOAN");
        assertThat(schedule.soChoCon).isEqualTo(8);
        verify(schedules).save(schedule);
    }

    @Test
    void customerCannotViewOtherCustomerBooking() {
        TaiKhoan userA = new TaiKhoan();
        userA.maTk = "TK-A";
        userA.tenDangNhap = "userA";

        KhachHang custA = new KhachHang();
        custA.maKh = "KH-A";
        custA.maTk = "TK-A";

        DatLich bookingOfB = new DatLich();
        bookingOfB.maDat = "DL-B";
        bookingOfB.maKh = "KH-B"; // belongs to customer B

        when(users.findByTenDangNhap("userA")).thenReturn(Optional.of(userA));
        when(customers.findByMaTk("TK-A")).thenReturn(Optional.of(custA));
        when(bookings.findById("DL-B")).thenReturn(Optional.of(bookingOfB));

        assertThatThrownBy(() -> bookingService.detail("userA", "DL-B"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bạn không có quyền truy cập đơn này");
    }

    @Test
    void customerCancelPreview_24HoursOrMore_Refund100Percent() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";

        KhachHang cust = new KhachHang();
        cust.maKh = "KH-1";
        cust.maTk = "TK-1";

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-FUTURE";
        schedule.ngay = LocalDate.now().plusDays(3);
        schedule.gioBatDau = LocalTime.of(10, 0);
        schedule.gioKetThuc = LocalTime.of(11, 30);

        DatLich booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maKh = "KH-1";
        booking.maLich = "L-FUTURE";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(cust));
        when(bookings.findById("DL-01")).thenReturn(Optional.of(booking));
        when(schedules.findById("L-FUTURE")).thenReturn(Optional.of(schedule));
        when(checkIns.existsByMaDat("DL-01")).thenReturn(false);

        CancellationPreviewResponse preview = bookingService.cancelPreview("customer1", "DL-01");

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.refundRate()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(preview.refundAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
    }

    @Test
    void customerCancelPreview_SixToUnderTwentyFourHours_Refunds50Percent() {
        CancellationPreviewResponse preview = previewForSchedule(LocalDateTime.now().plusHours(12));

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.refundRate()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(preview.refundAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(preview.message()).contains("6 đến dưới 24 giờ");
    }

    @Test
    void customerCancelPreview_UnderSixHours_DoesNotRefund() {
        CancellationPreviewResponse preview = previewForSchedule(LocalDateTime.now().plusHours(5));

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.refundRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.refundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.message()).contains("dưới 6 giờ");
    }

    @Test
    void customerCancelPreview_DuringSchedule_EligibleWithoutRefund() {
        LocalDateTime now = LocalDateTime.now();
        CancellationPreviewResponse preview = previewForSchedule(now.minusMinutes(15), now.plusMinutes(45));

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.refundRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.refundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.message()).contains("đang diễn ra");
    }

    @Test
    void customerCancel_DuringSchedule_DoesNotReleaseCapacity() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";
        KhachHang customer = new KhachHang();
        customer.maKh = "KH-1";
        customer.maTk = "TK-1";

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-ACTIVE";
        schedule.ngay = LocalDate.now();
        schedule.gioBatDau = LocalTime.MIN;
        schedule.gioKetThuc = LocalTime.MAX;
        schedule.sucChua = 20;
        schedule.soChoCon = 5;
        schedule.trangThai = TrangThaiLich.MO;

        DatLich booking = new DatLich();
        booking.maDat = "DL-ACTIVE";
        booking.maKh = "KH-1";
        booking.maLich = "L-ACTIVE";
        booking.soNguoi = 2;
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(customer));
        when(bookings.findById("DL-ACTIVE")).thenReturn(Optional.of(booking));
        when(bookings.lockById("DL-ACTIVE")).thenReturn(Optional.of(booking));
        when(schedules.findById("L-ACTIVE")).thenReturn(Optional.of(schedule));
        when(schedules.lockById("L-ACTIVE")).thenReturn(Optional.of(schedule));
        when(checkIns.existsByMaDat("DL-ACTIVE")).thenReturn(false);
        when(payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(
            "DL-ACTIVE", TrangThaiThanhToan.THANH_CONG
        )).thenReturn(Optional.empty());
        when(payments.findFirstByMaDatOrderByThoiDiemDesc("DL-ACTIVE")).thenReturn(Optional.empty());
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));
        when(catalog.schedule(schedule)).thenReturn(new ScheduleResponse(
            "L-ACTIVE", "DV-01", "Vé người lớn", BigDecimal.valueOf(50000),
            "KV-01", "Khu người lớn", "HB-01", "Hồ Thảo Điền",
            schedule.ngay, schedule.gioBatDau, schedule.gioKetThuc, 20, 5, "MO"
        ));

        BookingDetailResponse result = bookingService.cancel("customer1", "DL-ACTIVE", "Đổi kế hoạch");

        assertThat(result.trangThai()).isEqualTo("DA_HUY");
        assertThat(result.tyLeHoan()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.soTienHoan()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(schedule.soChoCon).isEqualTo(5);
        verify(schedules).save(schedule);
    }

    @Test
    void customerCancelPreview_AfterScheduleEnded_CannotCancel() {
        LocalDateTime now = LocalDateTime.now();
        CancellationPreviewResponse preview = previewForSchedule(now.minusHours(2), now.minusHours(1));

        assertThat(preview.eligible()).isFalse();
        assertThat(preview.refundRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.refundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(preview.message()).contains("đã kết thúc");
    }

    @Test
    void customerCancel_Success_UpdatesStatusAndRefund() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";

        KhachHang cust = new KhachHang();
        cust.maKh = "KH-1";
        cust.maTk = "TK-1";

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-FUTURE";
        schedule.ngay = LocalDate.now().plusDays(2);
        schedule.gioBatDau = LocalTime.of(10, 0);
        schedule.gioKetThuc = LocalTime.of(11, 30);
        schedule.sucChua = 20;
        schedule.soChoCon = 15;
        schedule.trangThai = TrangThaiLich.MO;

        DatLich booking = new DatLich();
        booking.maDat = "DL-01";
        booking.maKh = "KH-1";
        booking.maLich = "L-FUTURE";
        booking.soNguoi = 2;
        booking.donGiaTaiDat = BigDecimal.valueOf(50000);
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        ThanhToan payment = new ThanhToan();
        payment.maTt = "TT-01";
        payment.maDat = "DL-01";
        payment.soTien = BigDecimal.valueOf(100000);
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(cust));
        when(bookings.findById("DL-01")).thenReturn(Optional.of(booking));
        when(bookings.lockById("DL-01")).thenReturn(Optional.of(booking));
        when(schedules.findById("L-FUTURE")).thenReturn(Optional.of(schedule));
        when(schedules.lockById("L-FUTURE")).thenReturn(Optional.of(schedule));
        when(checkIns.existsByMaDat("DL-01")).thenReturn(false);
        when(payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc("DL-01", TrangThaiThanhToan.THANH_CONG))
            .thenReturn(Optional.of(payment));
        when(bookings.save(any(DatLich.class))).thenAnswer(inv -> inv.getArgument(0));
        when(catalog.schedule(any(LichDichVu.class))).thenReturn(new ScheduleResponse(
            "L-FUTURE", "DV-01", "Vé người lớn", BigDecimal.valueOf(50000),
            "KV-01", "Khu người lớn", "HB-01", "Hồ Thảo Điền",
            schedule.ngay, schedule.gioBatDau, LocalTime.of(11, 30), 20, 17, "MO"
        ));

        BookingDetailResponse res = bookingService.cancel("customer1", "DL-01", "Bận việc đột xuất");

        assertThat(res.trangThai()).isEqualTo("DA_HUY");
        assertThat(res.tyLeHoan()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(res.soTienHoan()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(res.thoiDiemBatDauApDung()).isEqualTo(LocalDateTime.of(schedule.ngay,schedule.gioBatDau));
        assertThat(res.trangThaiHoanTien()).isEqualTo("CHO_HOAN");
        assertThat(res.thoiGianGhiNhanHoan()).isNotNull();
        assertThat(res.maYeuCauHoan()).startsWith("REF-REQ-");
        assertThat(res.soTienYeuCauHoan()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(res.soTienHoanThucTe()).isNull();
        assertThat(schedule.soChoCon).isEqualTo(17); // returned 2 spots
        assertThat(payment.trangThaiHoan).isEqualTo("CHO_HOAN");
        assertThat(payment.soTienYeuCauHoan).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(payment.thoiGianYeuCauHoan).isNotNull();
        assertThat(payment.maYeuCauHoan).startsWith("REF-REQ-");
        assertThat(payment.soTienHoan).isNull();
        assertThat(payment.thoiGianHoan).isNull();
        assertThat(payment.maGiaoDichHoan).isNull();
        verify(payments).save(payment);
    }

    @Test
    void customerCancel_AlreadyCanceled_DoesNotRequestAnotherRefund() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";
        KhachHang customer = new KhachHang();
        customer.maKh = "KH-1";
        customer.maTk = "TK-1";
        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-CANCELED";
        DatLich booking = new DatLich();
        booking.maDat = "DL-CANCELED";
        booking.maKh = "KH-1";
        booking.maLich = "L-CANCELED";
        booking.soNguoi = 1;
        booking.donGiaTaiDat = BigDecimal.valueOf(50000);
        booking.tongTien = BigDecimal.valueOf(50000);
        booking.tyLeHoan = BigDecimal.valueOf(100);
        booking.soTienHoan = BigDecimal.valueOf(50000);
        booking.trangThai = TrangThaiDatLich.DA_HUY;
        ThanhToan payment = new ThanhToan();
        payment.maDat = "DL-CANCELED";
        payment.soTien = BigDecimal.valueOf(50000);
        payment.trangThai = TrangThaiThanhToan.THANH_CONG;
        payment.soTienYeuCauHoan = BigDecimal.valueOf(50000);
        payment.maYeuCauHoan = "REF-REQ-existing";
        payment.trangThaiHoan = "CHO_HOAN";

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(customer));
        when(bookings.findById("DL-CANCELED")).thenReturn(Optional.of(booking));
        when(schedules.lockById("L-CANCELED")).thenReturn(Optional.of(schedule));
        when(bookings.lockById("DL-CANCELED")).thenReturn(Optional.of(booking));
        when(schedules.findById("L-CANCELED")).thenReturn(Optional.of(schedule));
        when(payments.findFirstByMaDatOrderByThoiDiemDesc("DL-CANCELED")).thenReturn(Optional.of(payment));
        when(payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(
            "DL-CANCELED", TrangThaiThanhToan.THANH_CONG
        )).thenReturn(Optional.of(payment));
        when(catalog.schedule(schedule)).thenReturn(new ScheduleResponse(
            "L-CANCELED", "DV-01", "Vé người lớn", BigDecimal.valueOf(50000),
            "KV-01", "Khu người lớn", "HB-01", "Hồ Thảo Điền",
            LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0),
            20, 10, "MO"
        ));

        BookingDetailResponse result = bookingService.cancel("customer1", "DL-CANCELED", "Gửi lại");

        assertThat(result.trangThai()).isEqualTo("DA_HUY");
        assertThat(result.trangThaiHoanTien()).isEqualTo("CHO_HOAN");
        verify(bookings, never()).save(any(DatLich.class));
        verify(payments, never()).save(any(ThanhToan.class));
    }

    private CancellationPreviewResponse previewForSchedule(LocalDateTime startsAt, LocalDateTime endsAt) {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-1";
        user.tenDangNhap = "customer1";

        KhachHang customer = new KhachHang();
        customer.maKh = "KH-1";
        customer.maTk = "TK-1";

        LichDichVu schedule = new LichDichVu();
        schedule.maLich = "L-PREVIEW";
        schedule.ngay = startsAt.toLocalDate();
        schedule.gioBatDau = startsAt.toLocalTime();
        schedule.gioKetThuc = endsAt.toLocalTime();

        DatLich booking = new DatLich();
        booking.maDat = "DL-PREVIEW";
        booking.maKh = "KH-1";
        booking.maLich = "L-PREVIEW";
        booking.tongTien = BigDecimal.valueOf(100000);
        booking.trangThai = TrangThaiDatLich.DA_XAC_NHAN;

        when(users.findByTenDangNhap("customer1")).thenReturn(Optional.of(user));
        when(customers.findByMaTk("TK-1")).thenReturn(Optional.of(customer));
        when(bookings.findById("DL-PREVIEW")).thenReturn(Optional.of(booking));
        when(schedules.findById("L-PREVIEW")).thenReturn(Optional.of(schedule));
        when(checkIns.existsByMaDat("DL-PREVIEW")).thenReturn(false);

        return bookingService.cancelPreview("customer1", "DL-PREVIEW");
    }

    private CancellationPreviewResponse previewForSchedule(LocalDateTime startsAt) {
        return previewForSchedule(startsAt, startsAt.plusHours(1));
    }
}
