package com.poolbooking.security;

import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TaiKhoanRepository repo;

    public CustomUserDetailsService(TaiKhoanRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan user = repo.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(user.tenDangNhap)
                .password(user.matKhauBam)
                .roles(user.vaiTro.name())
                .disabled(!"HOAT_DONG".equals(user.trangThai))
                .build();
    }
}
