package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.PoolImageDtos.PoolImageResponse;
import com.poolbooking.service.PoolImageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
public class PoolImageController {
    private final PoolImageService images;
    public PoolImageController(PoolImageService images) { this.images = images; }

    @GetMapping("/api/pools/{poolId}/images")
    ApiResponse<List<PoolImageResponse>> list(@PathVariable String poolId) {
        return ApiResponse.ok("Thành công", images.list(poolId));
    }

    @PostMapping(value = "/api/admin/pools/{poolId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<List<PoolImageResponse>> upload(@PathVariable String poolId,
                                                @RequestParam("files") MultipartFile[] files) {
        return ApiResponse.created("Đã tải ảnh lên", images.upload(poolId, files));
    }

    @DeleteMapping("/api/admin/pool-images/{imageId}")
    ApiResponse<Void> delete(@PathVariable String imageId) {
        images.delete(imageId);
        return ApiResponse.ok("Đã xóa ảnh", null);
    }

    @PutMapping("/api/admin/pool-images/{imageId}/representative")
    ApiResponse<PoolImageResponse> representative(@PathVariable String imageId) {
        return ApiResponse.ok("Đã đặt ảnh đại diện", images.setRepresentative(imageId));
    }
}
