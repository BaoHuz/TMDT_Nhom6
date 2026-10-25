package com.poolbooking.controller;

import com.poolbooking.entity.BookingRequest;
import com.poolbooking.entity.BookingView;
import com.poolbooking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public BookingView create(@Valid @RequestBody BookingRequest request, Authentication authentication) {
        return service.view(service.create(authentication.getName(), request));
    }

    @GetMapping("/my")
    public List<BookingView> my(Authentication authentication) {
        return service.my(authentication.getName());
    }

    @PutMapping("/{id}/cancel")
    public Map<String, String> cancel(@PathVariable String id, Authentication authentication) {
        service.cancel(authentication.getName(), id);
        return Map.of("message", "Đã hủy đơn");
    }
}
