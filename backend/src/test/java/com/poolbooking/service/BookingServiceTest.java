package com.poolbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poolbooking.entity.BookingRequest;
import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.TrangThaiDatLich;
import com.poolbooking.enums.TrangThaiLich;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.DatLichRepository;
import com.poolbooking.repository.DichVuRepository;
import com.poolbooking.repository.KhuVucRepository;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.LichDichVuRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    private KhuVucRepository areaRepository;
    @Mock
    private KhachHangRepository customerRepository;
    @Mock
    private TaiKhoanRepository userRepository;

    private BookingService service;
    private LichDichVu schedule;

    @BeforeEach
    void setUp() {
        service = new BookingService(bookingRepository, scheduleRepository, serviceRepository, areaRepository,
                customerRepository, userRepository);
        schedule = new LichDichVu();
        schedule.maLich = "L001";
        schedule.maDv = "DV01";
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
        when(bookingRepository.findByMaLichAndTrangThaiOrderByNgayDatAsc("L001",
                TrangThaiDatLich.CHO_THANH_TOAN)).thenReturn(List.of());
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK01";
        when(userRepository.findByTenDangNhap("customer")).thenReturn(Optional.of(user));
        KhachHang customer = new KhachHang();
        customer.maKh = "KH01";
        when(customerRepository.findByMaTk("TK01")).thenReturn(Optional.of(customer));
        DichVu swimTicket = new DichVu();
        swimTicket.donGia = BigDecimal.valueOf(50000);
        when(serviceRepository.findById("DV01")).thenReturn(Optional.of(swimTicket));
        when(bookingRepository.save(any(DatLich.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, DatLich.class));

        DatLich booking = service.create("customer", new BookingRequest("L001", 1));

        assertEquals(0, schedule.soChoCon);
        assertEquals(TrangThaiLich.HET_CHO, schedule.trangThai);
        assertEquals(BigDecimal.valueOf(50000), booking.tongTien);
        assertEquals("KH01", booking.maKh);
        org.junit.jupiter.api.Assertions.assertTrue(booking.maDat.length() <= 20);
        verify(scheduleRepository).lockById("L001");
        verify(bookingRepository).save(booking);
    }

    @Test
    void rejectsMoreTicketsThanAreAvailable() {
        when(scheduleRepository.lockById("L001")).thenReturn(Optional.of(schedule));
        when(bookingRepository.findByMaLichAndTrangThaiOrderByNgayDatAsc("L001",
                TrangThaiDatLich.CHO_THANH_TOAN)).thenReturn(List.of());
        BusinessException error = assertThrows(BusinessException.class,
                () -> service.create("customer", new BookingRequest("L001", 2)));

        assertEquals("Không đủ vé còn trống. Vui lòng chọn số lượng khác", error.getMessage());
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
        expired.ngayDat = LocalDateTime.now().minusMinutes(6);
        when(bookingRepository.findByMaLichAndTrangThaiOrderByNgayDatAsc("L001",
                TrangThaiDatLich.CHO_THANH_TOAN)).thenReturn(List.of(expired), List.of());

        service.expirePendingBookings(schedule);
        service.expirePendingBookings(schedule);

        assertEquals(TrangThaiDatLich.HET_HAN, expired.trangThai);
        assertEquals(1, schedule.soChoCon);
        assertEquals(TrangThaiLich.MO, schedule.trangThai);
        verify(bookingRepository).save(expired);
    }
}
