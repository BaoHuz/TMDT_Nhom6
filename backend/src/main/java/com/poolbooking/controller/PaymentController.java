package com.poolbooking.controller;

import com.poolbooking.entity.BookingView;
import com.poolbooking.entity.PaymentRequest;
import com.poolbooking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public BookingView pay(@Valid @RequestBody PaymentRequest request, Authentication authentication) {
        return service.pay(authentication.getName(), request);
    }
}
