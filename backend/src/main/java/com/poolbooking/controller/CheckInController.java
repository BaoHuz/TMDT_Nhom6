package com.poolbooking.controller;

import com.poolbooking.entity.CheckInRequest;
import com.poolbooking.entity.DatLich;
import com.poolbooking.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-in")
public class CheckInController {
    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @PostMapping
    public DatLich check(@Valid @RequestBody CheckInRequest request, Authentication authentication) {
        return service.check(request.maVeQr(), authentication.getName());
    }
}
