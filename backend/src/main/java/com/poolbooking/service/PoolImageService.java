package com.poolbooking.service;

import com.poolbooking.dto.PoolImageDtos.PoolImageResponse;
import com.poolbooking.entity.HinhAnhHoBoi;
import com.poolbooking.exception.*;
import com.poolbooking.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PoolImageService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSIONS = Map.of(
        "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp"
    );

    private final HinhAnhHoBoiRepository images;
    private final HoBoiRepository pools;
    private final Path uploadRoot;

    public PoolImageService(HinhAnhHoBoiRepository images, HoBoiRepository pools,
                            @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.images = images;
        this.pools = pools;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<PoolImageResponse> list(String poolId) {
        if (!pools.existsById(poolId)) throw new ResourceNotFoundException("Không tìm thấy hồ bơi");
        return images.findByMaHoBoiOrderByLaAnhDaiDienDescThuTuAscNgayTaoAsc(poolId)
            .stream().map(this::response).toList();
    }

    @Transactional
    public List<PoolImageResponse> upload(String poolId, MultipartFile[] files) {
        if (!pools.existsById(poolId)) throw new ResourceNotFoundException("Không tìm thấy hồ bơi");
        if (files == null || files.length == 0) throw new BusinessException("Vui lòng chọn ít nhất một ảnh");
        if (files.length > MAX_FILES_PER_UPLOAD) throw new BusinessException("Chỉ được tải tối đa 10 ảnh mỗi lần");

        List<HinhAnhHoBoi> current = images.findByMaHoBoiOrderByLaAnhDaiDienDescThuTuAscNgayTaoAsc(poolId);
        int nextOrder = current.stream().mapToInt(x -> x.thuTu).max().orElse(-1) + 1;
        boolean needsRepresentative = current.stream().noneMatch(x -> x.laAnhDaiDien);
        Path poolDirectory = safePoolDirectory(poolId);
        List<Path> createdFiles = new ArrayList<>();
        List<HinhAnhHoBoi> createdImages = new ArrayList<>();

        try {
            Files.createDirectories(poolDirectory);
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String extension = validateAndGetExtension(file);
                String fileName = UUID.randomUUID() + "." + extension;
                Path target = poolDirectory.resolve(fileName).normalize();
                if (!target.startsWith(poolDirectory)) throw new BusinessException("Tên tệp không hợp lệ");
                try (InputStream input = file.getInputStream()) {
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                }
                createdFiles.add(target);

                HinhAnhHoBoi image = new HinhAnhHoBoi();
                image.maAnh = "ANH-" + UUID.randomUUID();
                image.maHoBoi = poolId;
                image.duongDan = "/uploads/pools/" + poolId + "/" + fileName;
                image.thuTu = nextOrder + i;
                image.laAnhDaiDien = needsRepresentative && i == 0;
                image.ngayTao = LocalDateTime.now();
                createdImages.add(image);
            }
            return images.saveAll(createdImages).stream().map(this::response).toList();
        } catch (IOException | RuntimeException e) {
            createdFiles.forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            });
            if (e instanceof BusinessException business) throw business;
            throw new BusinessException("Không thể lưu ảnh hồ bơi");
        }
    }

    @Transactional
    public void delete(String imageId) {
        HinhAnhHoBoi image = find(imageId);
        Path file = resolveStoredFile(image.duongDan);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new BusinessException("Không thể xóa tệp ảnh vật lý");
        }
        images.delete(image);

        if (image.laAnhDaiDien) {
            images.findByMaHoBoiOrderByLaAnhDaiDienDescThuTuAscNgayTaoAsc(image.maHoBoi)
                .stream().findFirst().ifPresent(next -> {
                    next.laAnhDaiDien = true;
                    images.save(next);
                });
        }
    }

    @Transactional
    public PoolImageResponse setRepresentative(String imageId) {
        HinhAnhHoBoi image = find(imageId);
        images.clearRepresentative(image.maHoBoi);
        image.laAnhDaiDien = true;
        return response(images.save(image));
    }

    private HinhAnhHoBoi find(String imageId) {
        return images.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh hồ bơi"));
    }

    private String validateAndGetExtension(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new BusinessException("Tệp ảnh không được để trống");
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException("Mỗi ảnh không được vượt quá 5 MB");

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        int dot = originalName.lastIndexOf('.');
        String extension = dot >= 0 ? originalName.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        String expectedType = EXTENSIONS.get(extension);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (expectedType == null || !ALLOWED_TYPES.contains(contentType) || !expectedType.equals(contentType)) {
            throw new BusinessException("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP");
        }

        byte[] header;
        try (InputStream input = file.getInputStream()) { header = input.readNBytes(12); }
        boolean valid = switch (contentType) {
            case "image/jpeg" -> header.length >= 3 && unsigned(header[0]) == 0xff && unsigned(header[1]) == 0xd8 && unsigned(header[2]) == 0xff;
            case "image/png" -> header.length >= 8 && unsigned(header[0]) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
            case "image/webp" -> header.length >= 12 && ascii(header, 0, "RIFF") && ascii(header, 8, "WEBP");
            default -> false;
        };
        if (!valid) throw new BusinessException("Nội dung tệp không phải ảnh hợp lệ");
        return extension.equals("jpeg") ? "jpg" : extension;
    }

    private Path safePoolDirectory(String poolId) {
        Path poolsDirectory = uploadRoot.resolve("pools").normalize();
        Path poolDirectory = poolsDirectory.resolve(poolId).normalize();
        if (!poolDirectory.startsWith(poolsDirectory)) throw new BusinessException("Mã hồ bơi không hợp lệ");
        return poolDirectory;
    }

    private Path resolveStoredFile(String storedPath) {
        String relative = storedPath.replaceFirst("^/uploads/", "");
        Path target = uploadRoot.resolve(relative).normalize();
        if (!target.startsWith(uploadRoot)) throw new BusinessException("Đường dẫn ảnh không hợp lệ");
        return target;
    }

    private int unsigned(byte value) { return value & 0xff; }
    private boolean ascii(byte[] bytes, int offset, String value) {
        if (bytes.length < offset + value.length()) return false;
        for (int i = 0; i < value.length(); i++) if (bytes[offset + i] != (byte) value.charAt(i)) return false;
        return true;
    }
    private PoolImageResponse response(HinhAnhHoBoi x) {
        return new PoolImageResponse(x.maAnh, x.maHoBoi, x.duongDan, x.thuTu, x.laAnhDaiDien, x.ngayTao);
    }
}
