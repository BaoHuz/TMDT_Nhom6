package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.CatalogDtos.*;
import com.poolbooking.service.CatalogService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog) { this.catalog = catalog; }
    @GetMapping("/pools") ApiResponse<List<PoolSummaryResponse>> pools() { return ApiResponse.ok("Thành công", catalog.pools()); }
    @GetMapping("/pools/{id}") ApiResponse<PoolDetailResponse> pool(@PathVariable String id) { return ApiResponse.ok("Thành công", catalog.pool(id)); }
    @GetMapping("/areas") ApiResponse<List<AreaResponse>> areas(@RequestParam(required=false) String poolId) { return ApiResponse.ok("Thành công", catalog.areas(poolId)); }
    @GetMapping("/services") ApiResponse<List<ServiceResponse>> services() { return ApiResponse.ok("Thành công", catalog.services()); }
}
