package com.poolbooking.service;

import com.poolbooking.dto.AuthDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.enums.VaiTro;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import com.poolbooking.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AuthService {
    private final TaiKhoanRepository users;
    private final KhachHangRepository customers;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(TaiKhoanRepository users, KhachHangRepository customers,
                       PasswordEncoder passwords, JwtService jwt) {
        this.users = users; this.customers = customers; this.passwords = passwords; this.jwt = jwt;
    }

    @Transactional
    public TokenData register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (users.existsByTenDangNhap(username)) throw new BusinessException("Tên đăng nhập đã tồn tại");
        if (users.existsByEmailIgnoreCase(email)) throw new BusinessException("Email đã được sử dụng");

        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-" + UUID.randomUUID(); user.tenDangNhap = username; user.email = email;
        user.matKhauBam = passwords.encode(request.password()); user.vaiTro = VaiTro.CUSTOMER;
        users.save(user);

        KhachHang customer = new KhachHang();
        customer.maKh = "KH-" + UUID.randomUUID(); customer.maTk = user.maTk;
        customer.hoTen = request.fullName().trim(); customer.dienThoai = request.phone(); customer.email = email;
        customers.save(customer);
        return tokens(user);
    }

    @Transactional(readOnly = true)
    public TokenData login(LoginRequest request) {
        String input = request.email().trim();
        TaiKhoan user = users.findByEmailIgnoreCase(input)
            .or(() -> users.findByTenDangNhap(input))
            .orElseThrow(() -> new BusinessException("Email hoặc mật khẩu không đúng"));
        if (!"HOAT_DONG".equals(user.trangThai) || !passwords.matches(request.password(), user.matKhauBam))
            throw new BusinessException("Email hoặc mật khẩu không đúng");
        return tokens(user);
    }

    @Transactional(readOnly = true)
    public TokenData refresh(RefreshRequest request) {
        String username;
        try { username = jwt.refreshSubject(request.refreshToken()); }
        catch (RuntimeException e) { throw new BusinessException("Refresh token không hợp lệ hoặc đã hết hạn"); }
        TaiKhoan user = users.findByTenDangNhap(username)
            .filter(x -> "HOAT_DONG".equals(x.trangThai))
            .orElseThrow(() -> new BusinessException("Tài khoản không còn hoạt động"));
        return tokens(user);
    }

    private TokenData tokens(TaiKhoan user) {
        String fullName = customers.findByMaTk(user.maTk).map(x -> x.hoTen).orElse(user.tenDangNhap);
        return new TokenData(jwt.createAccessToken(user), jwt.createRefreshToken(user),
            jwt.accessExpirationSeconds(), "Bearer", user.vaiTro.name(), fullName);
    }
}
