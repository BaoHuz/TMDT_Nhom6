package com.poolbooking.service;

import com.poolbooking.dto.BookingDtos.BookingRequest;
import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.*;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class BookingService {
    private final DatLichRepository bookings; private final LichDichVuRepository schedules;
    private final DichVuRepository services; private final TaiKhoanRepository users;
    private final KhachHangRepository customers; private final ThanhToanRepository payments;
    private final CheckInRepository checkIns; private final CatalogService catalog;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BookingService(DatLichRepository bookings, LichDichVuRepository schedules,
        DichVuRepository services, TaiKhoanRepository users, KhachHangRepository customers,
        ThanhToanRepository payments, CheckInRepository checkIns, CatalogService catalog,
        ApplicationEventPublisher eventPublisher) {
        this.bookings=bookings; this.schedules=schedules; this.services=services; this.users=users;
        this.customers=customers; this.payments=payments; this.checkIns=checkIns; this.catalog=catalog;
        this.eventPublisher=eventPublisher;
    }

    public BookingService(DatLichRepository bookings, LichDichVuRepository schedules,
        DichVuRepository services, TaiKhoanRepository users, KhachHangRepository customers,
        ThanhToanRepository payments, CheckInRepository checkIns, CatalogService catalog) {
        this(bookings,schedules,services,users,customers,payments,checkIns,catalog,event -> {});
    }

    @Transactional
    public BookingDetailResponse create(String username, BookingRequest request) {
        LichDichVu schedule=schedules.lockById(request.maLich()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        if(schedule.trangThai!=TrangThaiLich.MO) throw new BusinessException("Ca bơi không còn mở");
        if(LocalDateTime.of(schedule.ngay,schedule.gioBatDau).isBefore(LocalDateTime.now())) throw new BusinessException("Ca bơi đã bắt đầu");
        if(request.soNguoi()>schedule.soChoCon) throw new BusinessException("Không đủ chỗ trống");
        DichVu service=services.findById(schedule.maDv).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));
        DatLich booking=new DatLich(); booking.maDat="DL-"+UUID.randomUUID(); booking.maKh=currentCustomer(username).maKh;
        booking.maLich=schedule.maLich; booking.soNguoi=request.soNguoi(); booking.donGiaTaiDat=service.donGia;
        booking.tongTien=service.donGia.multiply(BigDecimal.valueOf(request.soNguoi())); booking.trangThai=TrangThaiDatLich.CHO_THANH_TOAN;
        booking.hanThanhToan=LocalDateTime.now().plusMinutes(15);
        schedule.soChoCon-=request.soNguoi(); if(schedule.soChoCon==0) schedule.trangThai=TrangThaiLich.HET_CHO;
        schedules.save(schedule); return detail(bookings.save(booking));
    }

    @Transactional(readOnly=true)
    public List<BookingSummaryResponse> mine(String username) {
        return bookings.findByMaKhOrderByNgayDatDesc(currentCustomer(username).maKh).stream().map(this::summary).toList();
    }

    @Transactional(readOnly=true) public BookingDetailResponse detail(String username,String id){return detail(owned(username,id));}

    @Transactional(readOnly=true)
    public CancellationPreviewResponse cancelPreview(String username,String id){return preview(owned(username,id));}

    @Transactional
    public BookingDetailResponse cancel(String username,String id,String reason){
        DatLich booking=bookings.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        assertOwner(username,booking);
        if(booking.trangThai==TrangThaiDatLich.DA_HUY) return detail(booking);
        LichDichVu lockedSchedule=schedules.lockById(booking.maLich).orElseThrow();
        CancellationPreviewResponse p=preview(booking); if(!p.eligible()) throw new BusinessException(p.message());
        LichDichVu schedule=schedules.lockById(booking.maLich).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        booking.trangThai=TrangThaiDatLich.DA_HUY; booking.lyDoHuy=reason==null?null:reason.trim(); booking.thoiGianHuy=LocalDateTime.now();
        booking.thoiDiemBatDauTinhHoan=LocalDateTime.of(lockedSchedule.ngay,lockedSchedule.gioBatDau);
        booking.tyLeHoan=p.refundRate(); booking.soTienHoan=p.refundAmount(); schedule.soChoCon=Math.min(schedule.sucChua,schedule.soChoCon+booking.soNguoi);
        if(schedule.trangThai==TrangThaiLich.HET_CHO) schedule.trangThai=TrangThaiLich.MO;
        payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(id,TrangThaiThanhToan.THANH_CONG).ifPresent(payment -> {
            payment.soTienHoan=p.refundAmount(); payment.thoiGianHoan=null;
        if(p.refundAmount().signum()>0) { payment.maGiaoDichHoan=UUID.randomUUID().toString().replace("-", ""); payment.trangThaiHoan=TrangThaiHoanTien.CHO_HOAN; payment.thoiGianHoan=null; }
        else payment.trangThaiHoan=TrangThaiHoanTien.KHONG_AP_DUNG;
        payments.save(payment);
        if(p.refundAmount().signum()>0 && "VNPAY".equalsIgnoreCase(payment.phuongThuc)) eventPublisher.publishEvent(new RefundRequestedEvent(payment.maTt));
        });
        schedules.save(schedule); return detail(bookings.save(booking));
    }

    @Transactional(readOnly=true)
    public DatLich owned(String username,String id){DatLich b=bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));assertOwner(username,b);return b;}

    private CancellationPreviewResponse preview(DatLich b){
        if(b.trangThai==TrangThaiDatLich.DA_HUY) return new CancellationPreviewResponse(false,0,nz(b.tyLeHoan),nz(b.soTienHoan),"Đơn đã được hủy trước đó");
        if(checkIns.existsByMaDat(b.maDat)||b.trangThai==TrangThaiDatLich.DA_CHECK_IN) return denied("Đơn đã check-in");
        if(b.trangThai!=TrangThaiDatLich.CHO_THANH_TOAN&&b.trangThai!=TrangThaiDatLich.DA_XAC_NHAN) return denied("Đơn không thể hủy ở trạng thái hiện tại");
        LichDichVu s=schedules.findById(b.maLich).orElseThrow(); long seconds=Duration.between(LocalDateTime.now(),LocalDateTime.of(s.ngay,s.gioBatDau)).getSeconds(); long hours=Math.floorDiv(seconds,3600);
        if(hours<0) return new CancellationPreviewResponse(false,hours,BigDecimal.ZERO,BigDecimal.ZERO,"Ca bơi đã bắt đầu");
        BigDecimal rate=seconds>=Duration.ofHours(24).getSeconds()?BigDecimal.valueOf(100):seconds>=Duration.ofHours(6).getSeconds()?BigDecimal.valueOf(50):BigDecimal.ZERO;
        BigDecimal amount=b.trangThai==TrangThaiDatLich.DA_XAC_NHAN?b.tongTien.multiply(rate).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):BigDecimal.ZERO;
        return new CancellationPreviewResponse(true,hours,rate,amount,rate.signum()==0?"Hủy hợp lệ nhưng không được hoàn tiền":"Hủy hợp lệ");
    }
    private CancellationPreviewResponse denied(String m){return new CancellationPreviewResponse(false,0,BigDecimal.ZERO,BigDecimal.ZERO,m);}
    private BigDecimal nz(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
    private void assertOwner(String username,DatLich b){if(!b.maKh.equals(currentCustomer(username).maKh))throw new BusinessException("Bạn không có quyền truy cập đơn này");}
    private KhachHang currentCustomer(String username){TaiKhoan u=users.findByTenDangNhap(username).orElseThrow();return customers.findByMaTk(u.maTk).orElseThrow(() -> new BusinessException("Tài khoản không có hồ sơ khách hàng"));}
    private BookingSummaryResponse summary(DatLich b){ScheduleResponse s=catalog.schedule(schedules.findById(b.maLich).orElseThrow());return new BookingSummaryResponse(b.maDat,b.maLich,s.dichVu(),s.hoBoi(),s.khuVuc(),s.ngay(),s.gioBatDau(),s.gioKetThuc(),b.soNguoi,b.tongTien,b.trangThai.name(),b.maVeQr);}
    @Transactional
    public void expireIfNeeded(DatLich booking) {
        if (booking.trangThai != TrangThaiDatLich.CHO_THANH_TOAN || booking.hanThanhToan == null || booking.hanThanhToan.isAfter(LocalDateTime.now())) return;
        LichDichVu schedule=schedules.lockById(booking.maLich).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        booking.trangThai=TrangThaiDatLich.HET_HAN;
        schedule.soChoCon=Math.min(schedule.sucChua,schedule.soChoCon+booking.soNguoi);
        if(schedule.trangThai==TrangThaiLich.HET_CHO && LocalDateTime.of(schedule.ngay,schedule.gioBatDau).isAfter(LocalDateTime.now())) schedule.trangThai=TrangThaiLich.MO;
        schedules.save(schedule); bookings.save(booking);
    }

    @Transactional
    public void expirePendingBookings() {
        bookings.findByTrangThaiAndHanThanhToanLessThanEqual(TrangThaiDatLich.CHO_THANH_TOAN,LocalDateTime.now()).forEach(b -> {
            DatLich locked=bookings.lockById(b.maDat).orElse(null);
            if(locked!=null) expireIfNeeded(locked);
        });
    }

    private BookingDetailResponse detail(DatLich b){ScheduleResponse s=catalog.schedule(schedules.findById(b.maLich).orElseThrow());ThanhToan p=payments.findFirstByMaDatOrderByThoiDiemDesc(b.maDat).orElse(null);String payment=p==null?"CHUA_THANH_TOAN":p.trangThai.name();String refund=p==null||p.trangThaiHoan==null?"KHONG_AP_DUNG":p.trangThaiHoan.name();return new BookingDetailResponse(b.maDat,b.maLich,s.dichVu(),s.hoBoi(),s.khuVuc(),s.ngay(),s.gioBatDau(),s.gioKetThuc(),b.soNguoi,b.donGiaTaiDat,b.tongTien,b.trangThai.name(),payment,b.maVeQr,b.lyDoHuy,b.thoiGianHuy,b.tyLeHoan,b.soTienHoan,b.hanThanhToan,refund,b.thoiDiemBatDauTinhHoan);}
}
