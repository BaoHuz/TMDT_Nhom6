package com.poolbooking.controller;

import com.poolbooking.entity.DatLich;
import com.poolbooking.entity.DichVu;
import com.poolbooking.entity.LichDichVu;
import com.poolbooking.entity.KhuVuc;
import com.poolbooking.entity.ScheduleRequest;
import com.poolbooking.entity.ServiceRequest;
import com.poolbooking.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/bookings")
    public List<DatLich> bookings() {
        return service.getBookings();
    }

    @GetMapping("/services")
    public List<DichVu> services() {
        return service.getServices();
    }

    @GetMapping("/areas")
    public List<KhuVuc> areas() {
        return service.getAreas();
    }

    @PostMapping("/services")
    public DichVu addService(@Valid @RequestBody ServiceRequest request) {
        return service.addService(request);
    }

    @PutMapping("/services/{id}")
    public DichVu updateService(@PathVariable String id, @Valid @RequestBody ServiceRequest request) {
        return service.updateService(id, request);
    }

    @GetMapping("/schedules")
    public List<LichDichVu> schedules() {
        return service.getSchedules();
    }

    @PostMapping("/schedules")
    public LichDichVu addSchedule(@Valid @RequestBody ScheduleRequest request) {
        return service.addSchedule(request);
    }

    @PutMapping("/schedules/{id}/close")
    public LichDichVu close(@PathVariable String id) {
        return service.closeSchedule(id);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }
}