package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.CatalogDtos.ScheduleResponse;
import com.poolbooking.service.ScheduleService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    private final ScheduleService schedules;
    public ScheduleController(ScheduleService schedules) { this.schedules = schedules; }
    @GetMapping ApiResponse<List<ScheduleResponse>> search(
        @RequestParam(required=false) LocalDate date, @RequestParam(required=false) String maHoBoi,
        @RequestParam(required=false) String maKhuVuc, @RequestParam(required=false) String maDv,
        @RequestParam(required=false) String sort) {
        return ApiResponse.ok("Lấy danh sách ca bơi thành công", schedules.search(date, maHoBoi, maKhuVuc, maDv, sort));
    }
    @GetMapping("/{id}") ApiResponse<ScheduleResponse> get(@PathVariable String id) {
        return ApiResponse.ok("Lấy ca bơi thành công", schedules.get(id));
    }
}
