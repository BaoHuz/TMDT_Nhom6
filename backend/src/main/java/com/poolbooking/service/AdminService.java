package com.poolbooking.service;

import com.poolbooking.dto.AdminDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.*;
import com.poolbooking.exception.*;
import com.poolbooking.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {
    private final DatLichRepository bookings; private final ThanhToanRepository payments; private final CheckInRepository checkIns;
    private final LichDichVuRepository schedules; private final DichVuRepository services; private final KhuVucRepository areas;
    private final HoBoiRepository pools; private final TaiKhoanRepository users; private final KhachHangRepository customers; private final PasswordEncoder encoder;
    private final ApplicationEventPublisher eventPublisher;
    public AdminService(DatLichRepository b,ThanhToanRepository p,CheckInRepository c,LichDichVuRepository s,DichVuRepository d,KhuVucRepository a,HoBoiRepository h,TaiKhoanRepository u,KhachHangRepository cs,PasswordEncoder e,ApplicationEventPublisher events){bookings=b;payments=p;checkIns=c;schedules=s;services=d;areas=a;pools=h;users=u;customers=cs;encoder=e;eventPublisher=events;}

    @Transactional(readOnly=true) public List<DatLich> bookings(){return bookings.findAll();}
    @Transactional(readOnly=true) public List<HoBoi> pools(){return pools.findAll();}
    @Transactional(readOnly=true) public List<KhuVuc> areas(){return areas.findAll();}
    @Transactional(readOnly=true) public List<DichVu> services(){return services.findAll();}
    @Transactional(readOnly=true) public List<LichDichVu> schedules(){return schedules.findAll();}
    @Transactional(readOnly=true) public List<AccountResponse> accounts(){return users.findAll().stream().map(this::account).toList();}

    @Transactional public HoBoi savePool(String id,PoolRequest r){HoBoi x=id==null?new HoBoi():pools.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ bơi"));if(!r.gioDongCua().isAfter(r.gioMoCua()))throw new BusinessException("Giờ đóng cửa phải sau giờ mở cửa");if(id==null)x.maHoBoi="HB-"+UUID.randomUUID();x.tenHoBoi=r.tenHoBoi();x.diaChi=r.diaChi();x.gioMoCua=r.gioMoCua();x.gioDongCua=r.gioDongCua();x.trangThai=valid(r.trangThai(),Set.of("HOAT_DONG","TAM_DONG","NGUNG_HOAT_DONG"),"HOAT_DONG");return pools.save(x);}
    @Transactional public KhuVuc saveArea(String id,AreaRequest r){if(!pools.existsById(r.maHoBoi()))throw new ResourceNotFoundException("Không tìm thấy hồ bơi");KhuVuc x=id==null?new KhuVuc():areas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khu vực"));if(id==null)x.maKhuVuc="KV-"+UUID.randomUUID();x.maHoBoi=r.maHoBoi();x.tenKhuVuc=r.tenKhuVuc();x.sucChua=r.sucChua();x.trangThai=valid(r.trangThai(),Set.of("HOAT_DONG","TAM_DONG","NGUNG_HOAT_DONG"),"HOAT_DONG");return areas.save(x);}
    @Transactional public DichVu saveService(String id,ServiceRequest r){DichVu x=id==null?new DichVu():services.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));if(id==null)x.maDv="DV-"+UUID.randomUUID();x.tenDv=r.tenDv();x.loaiDv=r.loaiDv();x.donGia=r.donGia();x.thoiLuongPhut=r.thoiLuongPhut();x.trangThai=valid(r.trangThai(),Set.of("DANG_BAN","TAM_DUNG","NGUNG_BAN"),"DANG_BAN");return services.save(x);}

    @Transactional public LichDichVu addSchedule(ScheduleRequest r){DichVu d=services.findById(r.maDv()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));if(!"DANG_BAN".equals(d.trangThai))throw new BusinessException("Dịch vụ đang ngừng bán");KhuVuc a=areas.findById(r.maKhuVuc()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khu vực"));if(!"HOAT_DONG".equals(a.trangThai))throw new BusinessException("Khu vực không hoạt động");HoBoi h=pools.findById(a.maHoBoi).orElseThrow();if(!"HOAT_DONG".equals(h.trangThai))throw new BusinessException("Hồ bơi không hoạt động");if(!r.gioKetThuc().isAfter(r.gioBatDau()))throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");if(r.gioBatDau().isBefore(h.gioMoCua)||r.gioKetThuc().isAfter(h.gioDongCua))throw new BusinessException("Ca phải nằm trong giờ mở cửa của hồ bơi");if(r.sucChua()>a.sucChua)throw new BusinessException("Sức chứa ca vượt sức chứa khu vực");LichDichVu x=new LichDichVu();x.maLich="L-"+UUID.randomUUID();x.maDv=r.maDv();x.maKhuVuc=r.maKhuVuc();x.ngay=r.ngay();x.gioBatDau=r.gioBatDau();x.gioKetThuc=r.gioKetThuc();x.sucChua=r.sucChua();x.soChoCon=r.sucChua();x.trangThai=TrangThaiLich.MO;return schedules.save(x);}
    @Transactional public LichDichVu closeSchedule(String id){LichDichVu x=schedules.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));x.trangThai=TrangThaiLich.DONG;return schedules.save(x);}
    @Transactional public LichDichVu cancelSchedule(String id,String reason){
        LichDichVu x=schedules.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca bơi"));
        x.trangThai=TrangThaiLich.DONG; schedules.save(x);
        List<DatLich> bList=bookings.findByMaLich(id); String r=reason==null||reason.isBlank()?"Ban quản lý hủy ca":reason.trim();
        for(DatLich b:bList){
            if(b.trangThai==TrangThaiDatLich.CHO_THANH_TOAN){
                b.trangThai=TrangThaiDatLich.DA_HUY; b.lyDoHuy=r; b.thoiGianHuy=java.time.LocalDateTime.now(); b.thoiDiemBatDauTinhHoan=java.time.LocalDateTime.of(x.ngay,x.gioBatDau);
                b.tyLeHoan=BigDecimal.ZERO; b.soTienHoan=BigDecimal.ZERO; bookings.save(b);
            } else if(b.trangThai==TrangThaiDatLich.DA_XAC_NHAN&&!checkIns.existsByMaDat(b.maDat)){
                b.trangThai=TrangThaiDatLich.DA_HUY; b.lyDoHuy=r; b.thoiGianHuy=java.time.LocalDateTime.now(); b.thoiDiemBatDauTinhHoan=java.time.LocalDateTime.of(x.ngay,x.gioBatDau);
                b.tyLeHoan=BigDecimal.valueOf(100); b.soTienHoan=b.tongTien; bookings.save(b);
                payments.findFirstByMaDatAndTrangThaiOrderByThoiDiemDesc(b.maDat,TrangThaiThanhToan.THANH_CONG).ifPresent(payment -> {
                    if(payment.soTienHoan==null||payment.soTienHoan.compareTo(b.tongTien)<0){
                        payment.soTienHoan=b.tongTien; payment.thoiGianHoan=null;
                        payment.trangThaiHoan=TrangThaiHoanTien.CHO_HOAN;
                        if(payment.maGiaoDichHoan==null) payment.maGiaoDichHoan=UUID.randomUUID().toString().replace("-", "");
                        payments.save(payment);
                        if("VNPAY".equalsIgnoreCase(payment.phuongThuc)) eventPublisher.publishEvent(new RefundRequestedEvent(payment.maTt));
                    }
                });
            }
        }
        return x;
    }
    @Transactional public AccountResponse createStaff(StaffRequest r){
        if(users.existsByTenDangNhap(r.username())||users.existsByEmailIgnoreCase(r.email()))throw new BusinessException("Tên đăng nhập hoặc email đã tồn tại");
        TaiKhoan u=new TaiKhoan();u.maTk="TK-"+UUID.randomUUID();u.tenDangNhap=r.username();u.email=r.email().toLowerCase();
        u.matKhauBam=encoder.encode(r.password());u.vaiTro=VaiTro.STAFF;
        TaiKhoan saved=users.save(u);
        KhachHang k=new KhachHang();k.maKh="KH-"+UUID.randomUUID();k.maTk=saved.maTk;k.hoTen=r.fullName().trim();k.email=saved.email;
        customers.save(k);
        return account(saved);
    }
    @Transactional public AccountResponse status(String id,boolean active){TaiKhoan u=users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));u.trangThai=active?"HOAT_DONG":"KHOA";return account(users.save(u));}
    @Transactional(readOnly=true) public Map<String,Object> dashboard(){BigDecimal revenue=payments.findByTrangThai(TrangThaiThanhToan.THANH_CONG).stream().map(x->x.soTien.subtract(x.soTienHoan==null?BigDecimal.ZERO:x.soTienHoan)).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("tongDon",bookings.count(),"doanhThu",revenue,"donDaXacNhan",bookings.countByTrangThai(TrangThaiDatLich.DA_XAC_NHAN),"donDaHuy",bookings.countByTrangThai(TrangThaiDatLich.DA_HUY),"luotCheckIn",checkIns.count(),"caDangMo",schedules.findByNgayAndTrangThai(LocalDate.now(),TrangThaiLich.MO).size());}
    private AccountResponse account(TaiKhoan u){return new AccountResponse(u.maTk,u.tenDangNhap,u.email,u.vaiTro.name(),u.trangThai,u.taoLuc);}
    private String valid(String v,Set<String> allowed,String fallback){String x=v==null||v.isBlank()?fallback:v;if(!allowed.contains(x))throw new BusinessException("Trạng thái không hợp lệ");return x;}
}
