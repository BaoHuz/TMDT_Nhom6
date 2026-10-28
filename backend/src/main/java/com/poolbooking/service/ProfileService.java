package com.poolbooking.service;

import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.entity.*;
import com.poolbooking.exception.BusinessException;
import com.poolbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final TaiKhoanRepository users; private final KhachHangRepository customers;
    public ProfileService(TaiKhoanRepository users,KhachHangRepository customers){this.users=users;this.customers=customers;}
    @Transactional(readOnly=true) public ProfileResponse get(String username){TaiKhoan u=user(username);KhachHang c=customer(u);return view(u,c);}
    @Transactional public ProfileResponse update(String username,ProfileUpdateRequest r){TaiKhoan u=user(username);KhachHang c=customer(u);if(r.fullName()==null||r.fullName().isBlank())throw new BusinessException("Họ tên không được để trống");c.hoTen=r.fullName().trim();c.dienThoai=r.phone()==null?null:r.phone().trim();return view(u,customers.save(c));}
    private TaiKhoan user(String username){return users.findByTenDangNhap(username).orElseThrow();}
    private KhachHang customer(TaiKhoan u){return customers.findByMaTk(u.maTk).orElseThrow(() -> new BusinessException("Không có hồ sơ khách hàng"));}
    private ProfileResponse view(TaiKhoan u,KhachHang c){return new ProfileResponse(u.tenDangNhap,c.hoTen,u.email,c.dienThoai,u.trangThai);}
}
