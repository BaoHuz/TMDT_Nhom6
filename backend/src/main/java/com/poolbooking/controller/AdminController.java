package com.poolbooking.controller;

import com.poolbooking.dto.*;
import com.poolbooking.dto.AdminDtos.*;
import com.poolbooking.dto.AdminDtos.ScheduleRequest;
import com.poolbooking.dto.AdminDtos.ServiceRequest;
import com.poolbooking.entity.*;
import com.poolbooking.service.AdminService;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;import java.util.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final AdminService a;public AdminController(AdminService a){this.a=a;}
 @GetMapping("/dashboard") ApiResponse<Map<String,Object>> dashboard(){return ApiResponse.ok("Thành công",a.dashboard());}
 @GetMapping("/bookings") ApiResponse<List<BookingResponse>> bookings(){return ApiResponse.ok("Thành công",a.bookings());}
 @GetMapping("/pools") ApiResponse<List<HoBoi>> pools(){return ApiResponse.ok("Thành công",a.pools());}
 @PostMapping("/pools") ApiResponse<HoBoi> pool(@Valid @RequestBody PoolRequest r){return ApiResponse.created("Đã tạo hồ bơi",a.savePool(null,r));}
 @PutMapping("/pools/{id}") ApiResponse<HoBoi> pool(@PathVariable String id,@Valid @RequestBody PoolRequest r){return ApiResponse.ok("Đã cập nhật",a.savePool(id,r));}
 @GetMapping("/areas") ApiResponse<List<KhuVuc>> areas(){return ApiResponse.ok("Thành công",a.areas());}
 @PostMapping("/areas") ApiResponse<KhuVuc> area(@Valid @RequestBody AreaRequest r){return ApiResponse.created("Đã tạo khu vực",a.saveArea(null,r));}
 @PutMapping("/areas/{id}") ApiResponse<KhuVuc> area(@PathVariable String id,@Valid @RequestBody AreaRequest r){return ApiResponse.ok("Đã cập nhật",a.saveArea(id,r));}
 @GetMapping("/services") ApiResponse<List<DichVu>> services(){return ApiResponse.ok("Thành công",a.services());}
 @PostMapping("/services") ApiResponse<DichVu> service(@Valid @RequestBody ServiceRequest r){return ApiResponse.created("Đã tạo dịch vụ",a.saveService(null,r));}
 @PutMapping("/services/{id}") ApiResponse<DichVu> service(@PathVariable String id,@Valid @RequestBody ServiceRequest r){return ApiResponse.ok("Đã cập nhật",a.saveService(id,r));}
 @GetMapping("/schedules") ApiResponse<List<LichDichVu>> schedules(){return ApiResponse.ok("Thành công",a.schedules());}
 @PostMapping("/schedules") ApiResponse<LichDichVu> schedule(@Valid @RequestBody ScheduleRequest r){return ApiResponse.created("Đã tạo ca",a.addSchedule(r));}
 @PutMapping("/schedules/{id}/close") ApiResponse<LichDichVu> close(@PathVariable String id){return ApiResponse.ok("Đã đóng ca",a.closeSchedule(id));}
 @PostMapping("/schedules/{id}/cancel") ApiResponse<LichDichVu> cancelSchedule(@PathVariable String id,@RequestBody(required=false) CancelScheduleRequest r){return ApiResponse.ok("Đã hủy ca bơi",a.cancelSchedule(id,r==null?null:r.reason()));}
 @GetMapping("/accounts") ApiResponse<List<AccountResponse>> accounts(){return ApiResponse.ok("Thành công",a.accounts());}
 @PostMapping("/accounts/staff") ApiResponse<AccountResponse> staff(@Valid @RequestBody StaffRequest r){return ApiResponse.created("Đã tạo STAFF",a.createStaff(r));}
 @PutMapping("/accounts/{id}/lock") ApiResponse<AccountResponse> lock(@PathVariable String id){return ApiResponse.ok("Đã khóa",a.status(id,false));}
 @PutMapping("/accounts/{id}/unlock") ApiResponse<AccountResponse> unlock(@PathVariable String id){return ApiResponse.ok("Đã mở khóa",a.status(id,true));}
}
