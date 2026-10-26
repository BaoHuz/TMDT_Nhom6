package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.service.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profiles; public ProfileController(ProfileService profiles){this.profiles=profiles;}
    @GetMapping ApiResponse<ProfileResponse> get(Authentication a){return ApiResponse.ok("Thành công",profiles.get(a.getName()));}
    @PutMapping ApiResponse<ProfileResponse> update(@RequestBody ProfileUpdateRequest r,Authentication a){return ApiResponse.ok("Đã cập nhật hồ sơ",profiles.update(a.getName(),r));}
}
