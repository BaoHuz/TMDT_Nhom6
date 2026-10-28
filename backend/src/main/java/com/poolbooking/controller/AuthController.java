package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.AuthDtos.*;
import com.poolbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<TokenData>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Đăng ký thành công", auth.register(request)));
    }

    @PostMapping("/login")
    ApiResponse<TokenData> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công", auth.login(request));
    }

    @PostMapping("/refresh")
    ApiResponse<TokenData> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok("Làm mới token thành công", auth.refresh(request));
    }
}
