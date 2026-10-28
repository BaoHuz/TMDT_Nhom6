package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.BookingDtos.CheckInRequest;
import com.poolbooking.entity.DatLich;
import com.poolbooking.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/check-in")
public class CheckInController {
    private final CheckInService checkIns;
    public CheckInController(CheckInService checkIns) { this.checkIns = checkIns; }
    @GetMapping("/preview")
    ApiResponse<DatLich> preview(@RequestParam String qr) {
        return ApiResponse.ok("Mã vé hợp lệ", checkIns.preview(qr));
    }
    @PostMapping
    ApiResponse<DatLich> check(@Valid @RequestBody CheckInRequest request, Authentication auth) {
        return ApiResponse.ok("Check-in thành công", checkIns.check(request.maVeQr(), auth.getName()));
    }
}
