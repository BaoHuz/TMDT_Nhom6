package com.poolbooking;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@SpringBootApplication @EnableMethodSecurity
public class PoolBookingApplication { public static void main(String[] a){org.springframework.boot.SpringApplication.run(PoolBookingApplication.class,a);} }

enum VaiTro { CUSTOMER, STAFF, ADMIN }
enum TrangThaiDatLich { CHO_THANH_TOAN, DA_XAC_NHAN, DA_HUY, DA_CHECK_IN }
enum TrangThaiThanhToan { THANH_CONG, THAT_BAI }
enum TrangThaiLich { MO, HET_CHO, DONG }

@Entity @Table(name="tai_khoan") class TaiKhoan { @Id public String maTk; @Column(unique=true,nullable=false) public String tenDangNhap; @Column(nullable=false) public String matKhauBam; public String hoTen,email,dienThoai; @Enumerated(EnumType.STRING) public VaiTro vaiTro=VaiTro.CUSTOMER; public String trangThai="HOAT_DONG"; public TaiKhoan(){} }
@Entity @Table(name="khu_vuc") class KhuVuc { @Id public String maKhuVuc; public String tenKhuVuc; public int sucChua; public String trangThai="HOAT_DONG"; public KhuVuc(){} }
@Entity @Table(name="dich_vu") class DichVu { @Id public String maDv; public String tenDv; @Column(precision=18,scale=2) public BigDecimal donGia; public int thoiLuongPhut; public String trangThai="DANG_BAN"; public DichVu(){} }
@Entity @Table(name="lich_dich_vu") class LichDichVu { @Id public String maLich; public String maDv,maKhuVuc; public LocalDate ngay; public LocalTime gioBatDau,gioKetThuc; public int sucChua,soChoCon; @Enumerated(EnumType.STRING) public TrangThaiLich trangThai=TrangThaiLich.MO; @Version @Column(name="row_version") public Long version; public LichDichVu(){} }
@Entity @Table(name="dat_lich") class DatLich { @Id public String maDat; public String maTk,maLich,maVeQr; public int soNguoi; public BigDecimal donGiaTaiDat,tongTien; public LocalDateTime ngayDat=LocalDateTime.now(); @Enumerated(EnumType.STRING) public TrangThaiDatLich trangThai=TrangThaiDatLich.CHO_THANH_TOAN; public DatLich(){} }
@Entity @Table(name="thanh_toan") class ThanhToan { @Id public String maThanhToan; public String maDat,phuongThuc,maGiaoDich; public BigDecimal soTien; public LocalDateTime ngayThanhToan=LocalDateTime.now(); @Enumerated(EnumType.STRING) public TrangThaiThanhToan trangThai; public ThanhToan(){} }
@Entity @Table(name="check_in") class CheckIn { @Id public String maCheckIn; @Column(unique=true) public String maDat; public String maTkNhanVien; public LocalDateTime thoiGian=LocalDateTime.now(); public CheckIn(){} }

interface TaiKhoanRepository extends JpaRepository<TaiKhoan,String>{Optional<TaiKhoan> findByTenDangNhap(String s);}
interface KhuVucRepository extends JpaRepository<KhuVuc,String>{}
interface DichVuRepository extends JpaRepository<DichVu,String>{}
interface LichDichVuRepository extends JpaRepository<LichDichVu,String>{List<LichDichVu> findByNgayAndTrangThai(LocalDate n,TrangThaiLich t);@Lock(LockModeType.PESSIMISTIC_WRITE)@Query("select l from LichDichVu l where l.maLich=:id")Optional<LichDichVu> lockById(@Param("id") String id);}
interface DatLichRepository extends JpaRepository<DatLich,String>{List<DatLich> findByMaTkOrderByNgayDatDesc(String id);Optional<DatLich> findByMaVeQr(String qr);long countByTrangThai(TrangThaiDatLich s);}
interface ThanhToanRepository extends JpaRepository<ThanhToan,String>{boolean existsByMaGiaoDich(String s);List<ThanhToan> findByTrangThai(TrangThaiThanhToan s);}
interface CheckInRepository extends JpaRepository<CheckIn,String>{boolean existsByMaDat(String id);long count();}

record RegisterRequest(@NotBlank String tenDangNhap,@NotBlank @Size(min=6) String matKhau,@NotBlank String hoTen,@Email String email,String dienThoai){}
record LoginRequest(@NotBlank String tenDangNhap,@NotBlank String matKhau){}
record BookingRequest(@NotBlank String maLich,@Min(1) int soNguoi){}
record PaymentRequest(@NotBlank String maDat){}
record CheckInRequest(@NotBlank String maVeQr){}
record ServiceRequest(@NotBlank String tenDv,@Min(0) BigDecimal donGia,@Min(1) int thoiLuongPhut){}
record ScheduleRequest(@NotBlank String maDv,@NotBlank String maKhuVuc,@NotNull LocalDate ngay,@NotNull LocalTime gioBatDau,@NotNull LocalTime gioKetThuc,@Min(1) int sucChua){}
record LoginResponse(String token,String role,String hoTen){}
record ApiError(boolean success,String message,Instant timestamp){}

@Service class JwtService {private final javax.crypto.SecretKey key; JwtService(@Value("${app.jwt.secret}") String secret){key=Keys.hmacShaKeyFor(secret.getBytes());} String create(TaiKhoan u){return Jwts.builder().subject(u.tenDangNhap).claim("role",u.vaiTro.name()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+86400000)).signWith(key).compact();} Claims parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}}
@Service class AccountService implements UserDetailsService {final TaiKhoanRepository repo;final PasswordEncoder encoder;AccountService(TaiKhoanRepository r,PasswordEncoder e){repo=r;encoder=e;}public UserDetails loadUserByUsername(String s){TaiKhoan u=repo.findByTenDangNhap(s).orElseThrow(()->new UsernameNotFoundException(s));return User.withUsername(u.tenDangNhap).password(u.matKhauBam).roles(u.vaiTro.name()).build();}}
class JwtFilter extends OncePerRequestFilter {final JwtService jwt;final AccountService users;JwtFilter(JwtService j,AccountService u){jwt=j;users=u;}protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest r,jakarta.servlet.http.HttpServletResponse p,jakarta.servlet.FilterChain c)throws IOException,jakarta.servlet.ServletException{String h=r.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer "))try{Claims x=jwt.parse(h.substring(7));var ud=users.loadUserByUsername(x.getSubject());SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities()));}catch(Exception ignored){}c.doFilter(r,p);}}

@Configuration class SecurityConfig { @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();} @Bean JwtFilter jwtFilter(JwtService j,AccountService u){return new JwtFilter(j,u);} @Bean SecurityFilterChain security(HttpSecurity h,JwtFilter f)throws Exception{return h.csrf(x->x.disable()).cors(x->{}).authorizeHttpRequests(x->x.requestMatchers("/api/auth/**","/api/schedules/**","/error").permitAll().requestMatchers("/api/check-in").hasAnyRole("STAFF","ADMIN").requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated()).addFilterBefore(f,UsernamePasswordAuthenticationFilter.class).build();} @Bean CorsConfigurationSource cors(){var c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:5173"));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}}

@Service class AuthService {final TaiKhoanRepository users;final PasswordEncoder encoder;final JwtService jwt;AuthService(TaiKhoanRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}LoginResponse register(RegisterRequest r){if(users.findByTenDangNhap(r.tenDangNhap()).isPresent())throw new BusinessException("Tên đăng nhập đã tồn tại");TaiKhoan u=new TaiKhoan();u.maTk="TK-"+UUID.randomUUID();u.tenDangNhap=r.tenDangNhap();u.matKhauBam=encoder.encode(r.matKhau());u.hoTen=r.hoTen();u.email=r.email();u.dienThoai=r.dienThoai();users.save(u);return new LoginResponse(jwt.create(u),u.vaiTro.name(),u.hoTen);}LoginResponse login(LoginRequest r){TaiKhoan u=users.findByTenDangNhap(r.tenDangNhap()).orElseThrow(()->new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));if(!encoder.matches(r.matKhau(),u.matKhauBam))throw new BusinessException("Sai tên đăng nhập hoặc mật khẩu");return new LoginResponse(jwt.create(u),u.vaiTro.name(),u.hoTen);}}
@Service class ScheduleService {final LichDichVuRepository ls;final DichVuRepository ds;final KhuVucRepository ks;ScheduleService(LichDichVuRepository l,DichVuRepository d,KhuVucRepository k){ls=l;ds=d;ks=k;}List<Map<String,Object>> all(LocalDate date){return ls.findByNgayAndTrangThai(date==null?LocalDate.now():date,TrangThaiLich.MO).stream().map(this::view).toList();}Map<String,Object> view(LichDichVu l){var m=new LinkedHashMap<String,Object>();m.put("maLich",l.maLich);m.put("ngay",l.ngay);m.put("gioBatDau",l.gioBatDau);m.put("gioKetThuc",l.gioKetThuc);m.put("soChoCon",l.soChoCon);m.put("sucChua",l.sucChua);m.put("trangThai",l.trangThai);m.put("dichVu",ds.findById(l.maDv).orElseThrow().tenDv);m.put("donGia",ds.findById(l.maDv).orElseThrow().donGia);m.put("khuVuc",ks.findById(l.maKhuVuc).orElseThrow().tenKhuVuc);return m;}}
@Service class BookingService {final DatLichRepository bs;final LichDichVuRepository ls;final DichVuRepository ds;final TaiKhoanRepository us;BookingService(DatLichRepository b,LichDichVuRepository l,DichVuRepository d,TaiKhoanRepository u){bs=b;ls=l;ds=d;us=u;}@Transactional DatLich create(String user,BookingRequest r){var l=ls.lockById(r.maLich()).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy ca bơi"));if(l.trangThai!=TrangThaiLich.MO)throw new BusinessException("Ca bơi không còn mở");if(r.soNguoi()>l.soChoCon)throw new BusinessException("Không đủ chỗ trống");var d=new DatLich();d.maDat="DL-"+UUID.randomUUID();d.maTk=us.findByTenDangNhap(user).orElseThrow().maTk;d.maLich=l.maLich;d.soNguoi=r.soNguoi();d.donGiaTaiDat=ds.findById(l.maDv).orElseThrow().donGia;d.tongTien=d.donGiaTaiDat.multiply(BigDecimal.valueOf(d.soNguoi));l.soChoCon-=d.soNguoi();if(l.soChoCon==0)l.trangThai=TrangThaiLich.HET_CHO;ls.save(l);return bs.save(d);}@Transactional List<DatLich> my(String user){String id=us.findByTenDangNhap(user).orElseThrow().maTk;return bs.findByMaTkOrderByNgayDatDesc(id);}@Transactional void cancel(String user,String id){var d=owned(user,id);if(d.trangThai!=TrangThaiDatLich.CHO_THANH_TOAN&&d.trangThai!=TrangThaiDatLich.DA_XAC_NHAN)throw new BusinessException("Đơn không thể hủy");var l=ls.lockById(d.maLich).orElseThrow();if(LocalDateTime.of(l.ngay,l.gioBatDau).isBefore(LocalDateTime.now()))throw new BusinessException("Đã quá giờ bắt đầu");d.trangThai=TrangThaiDatLich.DA_HUY;l.soChoCon+=d.soNguoi();if(l.trangThai==TrangThaiLich.HET_CHO)l.trangThai=TrangThaiLich.MO;ls.save(l);bs.save(d);}DatLich owned(String user,String id){var d=bs.findById(id).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy đơn"));if(!d.maTk.equals(us.findByTenDangNhap(user).orElseThrow().maTk))throw new BusinessException("Bạn không có quyền truy cập đơn này");return d;}}
@Service class PaymentService {final DatLichRepository bs;final ThanhToanRepository ps;PaymentService(DatLichRepository b,ThanhToanRepository p){bs=b;ps=p;}@Transactional DatLich pay(String user,PaymentRequest r){var d=bs.findById(r.maDat()).orElseThrow();if(!d.trangThai.equals(TrangThaiDatLich.CHO_THANH_TOAN))throw new BusinessException("Đơn không ở trạng thái chờ thanh toán");var p=new ThanhToan();p.maThanhToan="TT-"+UUID.randomUUID();p.maDat=d.maDat;p.maGiaoDich="SIM-"+UUID.randomUUID();p.soTien=d.tongTien;p.phuongThuc="MO_PHONG";p.trangThai=TrangThaiThanhToan.THANH_CONG;ps.save(p);d.trangThai=TrangThaiDatLich.DA_XAC_NHAN;d.maVeQr="QR-"+d.maDat;return bs.save(d);}}
@Service class CheckInService {final DatLichRepository bs;final CheckInRepository cs;final TaiKhoanRepository us;CheckInService(DatLichRepository b,CheckInRepository c,TaiKhoanRepository u){bs=b;cs=c;us=u;}@Transactional DatLich check(String qr,String staff){var d=bs.findByMaVeQr(qr).orElseThrow(()->new BusinessException("Mã QR không hợp lệ"));if(d.trangThai!=TrangThaiDatLich.DA_XAC_NHAN)throw new BusinessException("Đơn chưa đủ điều kiện check-in");if(cs.existsByMaDat(d.maDat))throw new BusinessException("Đơn đã check-in");var c=new CheckIn();c.maCheckIn="CI-"+UUID.randomUUID();c.maDat=d.maDat;c.maTkNhanVien=us.findByTenDangNhap(staff).orElseThrow().maTk;cs.save(c);d.trangThai=TrangThaiDatLich.DA_CHECK_IN;return bs.save(d);}}

@RestController @RequestMapping("/api/auth") class AuthController {final AuthService s;AuthController(AuthService x){s=x;}@PostMapping("/register") LoginResponse register(@Valid @RequestBody RegisterRequest r){return s.register(r);}@PostMapping("/login") LoginResponse login(@Valid @RequestBody LoginRequest r){return s.login(r);}}
@RestController @RequestMapping("/api/schedules") class ScheduleController {final ScheduleService s;ScheduleController(ScheduleService x){s=x;}@GetMapping List<Map<String,Object>> list(@RequestParam(required=false) LocalDate date){return s.all(date);}}
@RestController @RequestMapping("/api/bookings") class BookingController {final BookingService s;BookingController(BookingService x){s=x;}@PostMapping DatLich create(@Valid @RequestBody BookingRequest r,Authentication a){return s.create(a.getName(),r);}@GetMapping("/my") List<DatLich> my(Authentication a){return s.my(a.getName());}@PutMapping("/{id}/cancel") Map<String,String> cancel(@PathVariable String id,Authentication a){s.cancel(a.getName(),id);return Map.of("message","Đã hủy đơn");}}
@RestController @RequestMapping("/api/payments") class PaymentController {final PaymentService s;PaymentController(PaymentService x){s=x;}@PostMapping DatLich pay(@Valid @RequestBody PaymentRequest r,Authentication a){return s.pay(a.getName(),r);}}
@RestController @RequestMapping("/api/check-in") class CheckInController {final CheckInService s;CheckInController(CheckInService x){s=x;}@PostMapping DatLich check(@Valid @RequestBody CheckInRequest r,Authentication a){return s.check(r.maVeQr(),a.getName());}}
@RestController @RequestMapping("/api/admin") class AdminController {final DatLichRepository b;final ThanhToanRepository p;final CheckInRepository c;final LichDichVuRepository l;final DichVuRepository d;final KhuVucRepository k;AdminController(DatLichRepository x,ThanhToanRepository y,CheckInRepository z,LichDichVuRepository q,DichVuRepository dv,KhuVucRepository kv){b=x;p=y;c=z;l=q;d=dv;k=kv;}@GetMapping("/bookings")List<DatLich> bookings(){return b.findAll();}@GetMapping("/services")List<DichVu> services(){return d.findAll();}@PostMapping("/services")DichVu addService(@Valid @RequestBody ServiceRequest r){var x=new DichVu();x.maDv="DV-"+UUID.randomUUID();x.tenDv=r.tenDv();x.donGia=r.donGia();x.thoiLuongPhut=r.thoiLuongPhut();return d.save(x);}@PutMapping("/services/{id}")DichVu updateService(@PathVariable String id,@Valid @RequestBody ServiceRequest r){var x=d.findById(id).orElseThrow();x.tenDv=r.tenDv();x.donGia=r.donGia();x.thoiLuongPhut=r.thoiLuongPhut();return d.save(x);}@GetMapping("/schedules")List<LichDichVu> schedules(){return l.findAll();}@PostMapping("/schedules")LichDichVu addSchedule(@Valid @RequestBody ScheduleRequest r){var area=k.findById(r.maKhuVuc()).orElseThrow();if(r.sucChua()>area.sucChua)throw new BusinessException("Sức chứa ca vượt sức chứa khu vực");var x=new LichDichVu();x.maLich="L-"+UUID.randomUUID();x.maDv=r.maDv();x.maKhuVuc=r.maKhuVuc();x.ngay=r.ngay();x.gioBatDau=r.gioBatDau();x.gioKetThuc=r.gioKetThuc();x.sucChua=r.sucChua();x.soChoCon=r.sucChua();return l.save(x);}@PutMapping("/schedules/{id}/close")LichDichVu close(@PathVariable String id){var x=l.findById(id).orElseThrow();x.trangThai=TrangThaiLich.DONG;return l.save(x);}@GetMapping("/dashboard")Map<String,Object> dashboard(){var revenue=p.findByTrangThai(TrangThaiThanhToan.THANH_CONG).stream().map(x->x.soTien).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("tongDon",b.count(),"doanhThu",revenue,"luotCheckIn",c.count(),"caDangMo",l.findByNgayAndTrangThai(LocalDate.now(),TrangThaiLich.MO).size());}}

@RestControllerAdvice class GlobalExceptionHandler {@ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.BAD_REQUEST) ApiError handle(Exception e){return new ApiError(false,e.getMessage()==null?"Có lỗi xảy ra":e.getMessage(),Instant.now());}}
class BusinessException extends RuntimeException{BusinessException(String m){super(m);}} class ResourceNotFoundException extends RuntimeException{ResourceNotFoundException(String m){super(m);}}
