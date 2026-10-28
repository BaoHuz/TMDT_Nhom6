package com.poolbooking.security;

import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TaiKhoanRepository users;
    public CustomUserDetailsService(TaiKhoanRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        TaiKhoan user = users.findByTenDangNhap(username)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
        boolean enabled = "HOAT_DONG".equals(user.trangThai);
        return User.withUsername(user.tenDangNhap).password(user.matKhauBam)
            .roles(user.vaiTro.name()).disabled(!enabled).build();
    }
}
