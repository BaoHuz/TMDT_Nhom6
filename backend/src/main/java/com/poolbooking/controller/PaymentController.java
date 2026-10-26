package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.BookingDtos.PaymentRequest;
import com.poolbooking.entity.DatLich;
import com.poolbooking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;
    public PaymentController(PaymentService payments) { this.payments = payments; }
    @PostMapping
    ApiResponse<DatLich> pay(@Valid @RequestBody PaymentRequest request, Authentication auth) {
        return ApiResponse.ok("Thanh toán mô phỏng thành công", payments.pay(auth.getName(), request));
    }
}
