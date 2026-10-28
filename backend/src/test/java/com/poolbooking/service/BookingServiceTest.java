package com.poolbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poolbooking.dto.BookingDtos.BookingRequest;
import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.dto.CustomerDtos.BookingDetailResponse;
import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.CheckInRepository;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.DichVuRepository;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private DatLichRepository bookingRepository;
    @Mock
    private LichDichVuRepository scheduleRepository;
    @Mock
    private DichVuRepository serviceRepository;
    @Mock
    private KhachHangRepository customerRepository;
    @Mock
    private TaiKhoanRepository userRepository;
    @Mock
    private com.poolbooking.repository.ThanhToanRepository paymentRepository;
    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private CatalogService catalog;

    private BookingService service;
    private LichDichVu schedule;

    @BeforeEach
    void setUp() {
        service = new BookingService(bookingRepository, scheduleRepository, serviceRepository, userRepository,
                customerRepository, paymentRepository, checkInRepository, catalog);
        schedule = new LichDichVu();
        schedule.maLich = "L001";
        schedule.maDv = "DV01";
        schedule.maKhuVuc = "KV01";
        schedule.ngay = LocalDate.now().plusDays(7);
        schedule.gioBatDau = LocalTime.of(8, 0);
        schedule.gioKetThuc = LocalTime.of(9, 30);
        schedule.sucChua = 1;
        schedule.soChoCon = 1;
        schedule.trangThai = TrangThaiLich.MO;
    }

    @Test
    void reservesTheLastAvailableTicket() {
        when(scheduleRepository.lockById("L001")).thenReturn(Optional.of(schedule));
        when(scheduleRepository.findById("L001")).thenReturn(Optional.of(schedule));
        when(bookingRepository.save(any(DatLich.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, DatLich.class));
        when(userRepository.findByTenDangNhap("customer")).thenReturn(Optional.of(user()));
        when(customerRepository.findByMaTk("TK01")).thenReturn(Optional.of(customer()));
        when(serviceRepository.findById("DV01")).thenReturn(Optional.of(service()));
        when(catalog.schedule(schedule)).thenReturn(new ScheduleResponse("L001", "DV01", "Bơi", BigDecimal.valueOf(50000),
                "KV01", "Khu vực", "HB01", "Hồ bơi", schedule.ngay, schedule.gioBatDau,
                schedule.gioKetThuc, 1, 1, "MO"));
        when(paymentRepository.findFirstByMaDatOrderByThoiDiemDesc(anyString())).thenReturn(Optional.empty());

        BookingDetailResponse booking = service.create("customer", new BookingRequest("L001", 1));

        assertEquals(0, schedule.soChoCon);
        assertEquals(TrangThaiLich.HET_CHO, schedule.trangThai);
        assertEquals(BigDecimal.valueOf(50000), booking.tongTien());
        assertEquals("L001", booking.maLich());
        verify(scheduleRepository).lockById("L001");
        verify(scheduleRepository).save(schedule);
        verify(bookingRepository).save(any(DatLich.class));
    }

    @Test
    void rejectsMoreTicketsThanAreAvailable() {
        when(scheduleRepository.lockById("L001")).thenReturn(Optional.of(schedule));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.create("customer", new BookingRequest("L001", 2)));

        assertEquals("Không đủ chỗ trống", error.getMessage());
        assertEquals(1, schedule.soChoCon);
        verify(bookingRepository, never()).save(any(DatLich.class));
    }

    @Test
    void expiresAnUnpaidHoldAndReleasesItsTicketOnlyOnce() {
        schedule.soChoCon = 0;
        schedule.trangThai = TrangThaiLich.HET_CHO;
        DatLich expired = new DatLich();
        expired.maDat = "DL-OLD";
        expired.maLich = schedule.maLich;
        expired.soNguoi = 1;
        expired.hanThanhToan = LocalDateTime.now().minusMinutes(1);
        expired.trangThai = TrangThaiDatLich.CHO_THANH_TOAN;
        when(scheduleRepository.lockById("L001")).thenReturn(Optional.of(schedule));

        service.expireIfNeeded(expired);
        service.expireIfNeeded(expired);

        assertEquals(TrangThaiDatLich.HET_HAN, expired.trangThai);
        assertEquals(1, schedule.soChoCon);
        assertEquals(TrangThaiLich.MO, schedule.trangThai);
        verify(bookingRepository).save(expired);
        verify(scheduleRepository).save(schedule);
    }

    private TaiKhoan user() {
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK01";
        user.tenDangNhap = "customer";
        return user;
    }

    private KhachHang customer() {
        KhachHang customer = new KhachHang();
        customer.maKh = "KH01";
        customer.maTk = "TK01";
        return customer;
    }

    private DichVu service() {
        DichVu service = new DichVu();
        service.maDv = "DV01";
        service.donGia = BigDecimal.valueOf(50000);
        return service;
    }
}
