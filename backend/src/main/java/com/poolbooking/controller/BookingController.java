package com.poolbooking.controller;

import com.poolbooking.dto.*;
import com.poolbooking.dto.BookingDtos.BookingRequest;
import com.poolbooking.dto.CustomerDtos.*;
import com.poolbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookings; public BookingController(BookingService bookings){this.bookings=bookings;}
    @PostMapping ResponseEntity<ApiResponse<BookingDetailResponse>> create(@Valid @RequestBody BookingRequest r,Authentication a){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tạo đơn thành công",bookings.create(a.getName(),r)));}
    @GetMapping("/my") ApiResponse<List<BookingSummaryResponse>> mine(Authentication a){return ApiResponse.ok("Thành công",bookings.mine(a.getName()));}
    @GetMapping("/{id}") ApiResponse<BookingDetailResponse> detail(@PathVariable String id,Authentication a){return ApiResponse.ok("Thành công",bookings.detail(a.getName(),id));}
    @GetMapping("/{id}/cancel-preview") ApiResponse<CancellationPreviewResponse> preview(@PathVariable String id,Authentication a){return ApiResponse.ok("Thành công",bookings.cancelPreview(a.getName(),id));}
    @PostMapping("/{id}/cancel") ApiResponse<BookingDetailResponse> cancel(@PathVariable String id,@RequestBody(required=false) CancelRequest r,Authentication a){return ApiResponse.ok("Đã hủy đơn",bookings.cancel(a.getName(),id,r==null?null:r.reason()));}
}
