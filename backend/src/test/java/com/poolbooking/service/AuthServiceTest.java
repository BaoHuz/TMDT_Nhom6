package com.poolbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.poolbooking.dto.AuthDtos.LoginRequest;
import com.poolbooking.dto.AuthDtos.TokenData;
import com.poolbooking.entity.KhachHang;
import com.poolbooking.entity.TaiKhoan;
import com.poolbooking.enums.VaiTro;
import com.poolbooking.repository.KhachHangRepository;
import com.poolbooking.repository.TaiKhoanRepository;
import com.poolbooking.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

class AuthServiceTest {
    @Test
    void authenticatesDemoAccountWithPrefixedBcryptHash() {
        TaiKhoanRepository users = mock(TaiKhoanRepository.class);
        KhachHangRepository customers = mock(KhachHangRepository.class);
        TaiKhoan user = new TaiKhoan();
        user.maTk = "TK-CUSTOMER";
        user.tenDangNhap = "customer";
        user.email = "customer@example.com";
        user.trangThai = "HOAT_DONG";
        user.vaiTro = VaiTro.CUSTOMER;
        user.matKhauBam = "{bcrypt}$2a$10$iHnuvcu3yObLcSDyzML3ouhYEDFsMlL9aEsJCUVm8JLVt0rsMfufW";
        when(users.findByEmailIgnoreCase("customer")).thenReturn(Optional.empty());
        when(users.findByTenDangNhap("customer")).thenReturn(Optional.of(user));
        KhachHang customer = new KhachHang();
        customer.maTk = user.maTk;
        customer.hoTen = "Nguyen Van A";
        when(customers.findByMaTk(user.maTk)).thenReturn(Optional.of(customer));

        AuthService service = new AuthService(users, customers,
                PasswordEncoderFactories.createDelegatingPasswordEncoder(),
                new JwtService("A".repeat(64), 900, 604800));

        TokenData response = service.login(new LoginRequest("customer", "password"));

        assertEquals("CUSTOMER", response.role());
        assertEquals("Nguyen Van A", response.fullName());
        assertFalse(response.accessToken().isBlank());
        assertFalse(response.refreshToken().isBlank());
    }
}
