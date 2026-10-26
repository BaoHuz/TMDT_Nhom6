package com.poolbooking.security;

import com.poolbooking.entity.TaiKhoan;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-seconds:900}") long accessExpirationSeconds,
                      @Value("${app.jwt.refresh-expiration-seconds:604800}") long refreshExpirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String createAccessToken(TaiKhoan user) { return create(user, "access", accessExpirationSeconds); }
    public String createRefreshToken(TaiKhoan user) { return create(user, "refresh", refreshExpirationSeconds); }
    public long accessExpirationSeconds() { return accessExpirationSeconds; }

    private String create(TaiKhoan user, String type, long lifetimeSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.tenDangNhap)
            .claim("role", user.vaiTro.name())
            .claim("type", type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(lifetimeSeconds)))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String accessSubject(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("type", String.class))) throw new JwtException("Token không hợp lệ");
        return claims.getSubject();
    }

    public String refreshSubject(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("type", String.class))) throw new JwtException("Refresh token không hợp lệ");
        return claims.getSubject();
    }
}
