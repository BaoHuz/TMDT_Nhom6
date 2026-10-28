package com.poolbooking.dto;

import java.time.LocalDateTime;

public final class PoolImageDtos {
    private PoolImageDtos() {}

    public record PoolImageResponse(String maAnh, String maHoBoi, String duongDan,
                                    int thuTu, boolean laAnhDaiDien, LocalDateTime ngayTao) {}
}
