package com.poolbooking.service;

import com.poolbooking.dto.BookingDtos.BookingRequest;
import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.*;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookingService {
    private final DatLichRepository bookings; private final LichDichVuRepository schedules;
    private final DichVuRepository services; private final TaiKhoanRepository users;
    private final KhachHangRepository customers; private final ThanhToanRepository payments;
    private final CheckInRepository checkIns; private final CatalogService catalog;

    public BookingService(DatLichRepository bookings, LichDichVuRepository schedules,
        DichVuRepository services, TaiKhoanRepository users, KhachHangRepository customers,
        ThanhToanRepository payments, CheckInRepository checkIns, CatalogService catalog) {
        this.bookings=bookings; this.schedules=schedules; this.services=services; this.users=users;
        this.customers=customers; this.payments=payments; this.checkIns=checkIns; this.catalog=catalog;
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
        DatLich existing=bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        assertOwner(username,existing);
        LichDichVu schedule=schedules.lockById(existing.maLich)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        DatLich booking=bookings.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        assertOwner(username,booking);
        if(booking.trangThai==TrangThaiDatLich.DA_HUY) return detail(booking);
        CancellationPreviewResponse p=preview(booking,schedule,LocalDateTime.now());
        if(!p.eligible()) throw new BusinessException(p.message());
        booking.trangThai=TrangThaiDatLich.DA_HUY; booking.lyDoHuy=reason==null?null:reason.trim(); booking.thoiGianHuy=LocalDateTime.now();
        booking.tyLeHoan=p.refundRate(); booking.soTienHoan=p.refundAmount();
        if(p.refundRate().signum()>0){
            schedule.soChoCon=Math.min(schedule.sucChua,schedule.soChoCon+booking.soNguoi);
            if(schedule.trangThai==TrangThaiLich.HET_CHO) schedule.trangThai=TrangThaiLich.MO;
        }
        payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(id,TrangThaiThanhToan.THANH_CONG).ifPresent(payment -> {
            payment.soTienHoan=p.refundAmount();
            payment.trangThaiHoan=p.refundAmount().signum()>0?"CHO_HOAN":"KHONG_HOAN";
            if(p.refundAmount().signum()>0){
                payment.thoiGianHoan=LocalDateTime.now();
                payment.maGiaoDichHoan="REF-REQ-"+UUID.randomUUID();
            } else {
                payment.thoiGianHoan=null;
                payment.maGiaoDichHoan=null;
            }
            payments.save(payment);
        });
        schedules.save(schedule); return detail(bookings.save(booking));
    }

    @Transactional(readOnly=true)
    public DatLich owned(String username,String id){DatLich b=bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));assertOwner(username,b);return b;}

    private CancellationPreviewResponse preview(DatLich b){
        if(b.trangThai==TrangThaiDatLich.DA_HUY) return new CancellationPreviewResponse(false,0,nz(b.tyLeHoan),nz(b.soTienHoan),"Đơn đã được hủy trước đó");
        LichDichVu s=schedules.findById(b.maLich).orElseThrow();
        return preview(b,s,LocalDateTime.now());
    }

    private CancellationPreviewResponse preview(DatLich b,LichDichVu s,LocalDateTime now){
        if(checkIns.existsByMaDat(b.maDat)||b.trangThai==TrangThaiDatLich.DA_CHECK_IN) return denied("Đơn đã check-in");
        if(b.trangThai!=TrangThaiDatLich.CHO_THANH_TOAN&&b.trangThai!=TrangThaiDatLich.DA_XAC_NHAN) return denied("Đơn không thể hủy ở trạng thái hiện tại");
        LocalDateTime startsAt=LocalDateTime.of(s.ngay,s.gioBatDau);
        LocalDateTime endsAt=LocalDateTime.of(s.ngay,s.gioKetThuc);
        long hours=ChronoUnit.HOURS.between(now,startsAt);
        if(!now.isBefore(endsAt)) return new CancellationPreviewResponse(false,hours,BigDecimal.ZERO,BigDecimal.ZERO,"Ca bơi đã kết thúc, không thể hủy vé");
        boolean duringSession=!now.isBefore(startsAt);
        Duration timeUntilStart=Duration.between(now,startsAt);
        BigDecimal rate=duringSession||timeUntilStart.compareTo(Duration.ofHours(6))<0?BigDecimal.ZERO
            :timeUntilStart.compareTo(Duration.ofHours(24))<0?BigDecimal.valueOf(50):BigDecimal.valueOf(100);
        BigDecimal amount=b.trangThai==TrangThaiDatLich.DA_XAC_NHAN?b.tongTien.multiply(rate).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):BigDecimal.ZERO;
        String message=duringSession?"Ca bơi đang diễn ra: hủy vé không được hoàn tiền"
            :rate.compareTo(BigDecimal.valueOf(100))==0?"Hủy trước ca bơi từ 24 giờ: được hoàn 100% tiền vé"
            :rate.compareTo(BigDecimal.valueOf(50))==0?"Hủy trước ca bơi từ 6 đến dưới 24 giờ: được hoàn 50% tiền vé"
            :"Hủy trước ca bơi dưới 6 giờ: không được hoàn tiền";
        return new CancellationPreviewResponse(true,hours,rate,amount,message);
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

    private BookingDetailResponse detail(DatLich b){
        ScheduleResponse s=catalog.schedule(schedules.findById(b.maLich).orElseThrow());
        String payment=payments.findFirstByMaDatOrderByThoiDiemDesc(b.maDat)
            .map(x->x.trangThai.name()).orElse("CHUA_THANH_TOAN");
        ThanhToan refundPayment=payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(
            b.maDat,TrangThaiThanhToan.THANH_CONG).orElse(null);
        String refund=refundPayment==null||refundPayment.trangThaiHoan==null
            ?"KHONG_AP_DUNG":refundPayment.trangThaiHoan;
        return new BookingDetailResponse(b.maDat,b.maLich,s.dichVu(),s.hoBoi(),s.khuVuc(),s.ngay(),
            s.gioBatDau(),s.gioKetThuc(),b.soNguoi,b.donGiaTaiDat,b.tongTien,b.trangThai.name(),
            payment,refund,refundPayment==null?null:refundPayment.thoiGianHoan,
            refundPayment==null?null:refundPayment.maGiaoDichHoan,
            b.maVeQr,b.lyDoHuy,b.thoiGianHuy,b.tyLeHoan,b.soTienHoan,b.hanThanhToan);
    }
}
