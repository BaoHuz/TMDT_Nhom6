package com.poolbooking.service;

import com.poolbooking.entity.LoginRequest;
import com.poolbooking.entity.LoginResponse;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.RegisterRequest;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import com.poolbooking.security.JwtService;
import java.util.UUID;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final TaiKhoanRepository users;
    private final KhachHangRepository customers;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(TaiKhoanRepository users, KhachHangRepository customers, PasswordEncoder encoder,
            JwtService jwtService) {
        this.users = users;
        this.customers = customers;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (users.findByTenDangNhap(request.tenDangNhap()).isPresent()) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        TaiKhoan user = new TaiKhoan();
        user.maTk = newId("TK-");
        user.tenDangNhap = request.tenDangNhap();
        user.matKhauBam = encoder.encode(request.matKhau());
        users.save(user);

        KhachHang customer = new KhachHang();
        customer.maKh = newId("KH-");
        customer.maTk = user.maTk;
        customer.hoTen = request.hoTen();
        customer.email = request.email();
        customer.dienThoai = request.dienThoai();
        customers.save(customer);
        return new LoginResponse(jwtService.create(user), user.vaiTro.name(), customer.hoTen);
    }

    public LoginResponse login(LoginRequest request) {
        TaiKhoan user = users.findByTenDangNhap(request.tenDangNhap())
                .orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!"HOAT_DONG".equals(user.trangThai)) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }
        String encodedPassword = user.matKhauBam;
        if (encodedPassword.startsWith("{bcrypt}")) {
            encodedPassword = encodedPassword.substring("{bcrypt}".length());
        }
        if (!encoder.matches(request.matKhau(), encodedPassword)) {
            throw new BusinessException("Sai tên đăng nhập hoặc mật khẩu");
        }
        KhachHang customer = customers.findByMaTk(user.maTk).orElse(null);
        if (customer != null && !"HOAT_DONG".equals(customer.trangThai)) {
            throw new BusinessException("Tài khoản khách hàng đã bị khóa");
        }
        String displayName = customer == null ? user.tenDangNhap : customer.hoTen;
        return new LoginResponse(jwtService.create(user), user.vaiTro.name(), displayName);
    }

    private String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
