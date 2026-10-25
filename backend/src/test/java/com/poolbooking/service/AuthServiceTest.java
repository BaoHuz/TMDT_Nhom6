package com.poolbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.poolbooking.entity.LoginRequest;
import com.poolbooking.entity.LoginResponse;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import com.poolbooking.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    @Test
    void authenticatesDemoAccountWithPrefixedBcryptHash() {
        TaiKhoanRepository users = mock(TaiKhoanRepository.class);
        KhachHangRepository customers = mock(KhachHangRepository.class);
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-CUSTOMER";
        user.tenDangNhap = "customer";
        user.matKhauBam = "{bcrypt}$2a$10$iHnuvcu3yObLcSDyzML3ouhYEDFsMlL9aEsJCUVm8JLVt0rsMfufW";
        when(users.findByTenDangNhap("customer")).thenReturn(Optional.of(user));
        KhachHang customer = new KhachHang();
        customer.maTk = user.maTk;
        customer.hoTen = "Nguyen Van A";
        when(customers.findByMaTk(user.maTk)).thenReturn(Optional.of(customer));

        AuthService service = new AuthService(users, customers, new BCryptPasswordEncoder(),
                new JwtService("A".repeat(64)));

        LoginResponse response = service.login(new LoginRequest("customer", "password"));

        assertEquals("CUSTOMER", response.role());
        assertEquals("Nguyen Van A", response.hoTen());
        org.junit.jupiter.api.Assertions.assertFalse(response.token().isBlank());
    }
}
