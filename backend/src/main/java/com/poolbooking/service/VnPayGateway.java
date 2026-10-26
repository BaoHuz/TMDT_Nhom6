package com.poolbooking.service;

import com.poolbooking.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VnPayGateway {
    @Value("${vnpay.vnp_PayUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") private String payUrl;
    @Value("${app.vnpay.tmn-code:}") private String tmnCode;
    @Value("${app.vnpay.hash-secret:}") private String hashSecret;
    @Value("${vnpay.vnp_ReturnUrl:http://localhost:8080/api/payments/vnpay/return}") private String returnUrl;

    public String createPaymentUrl(String txnRef, BigDecimal amount, String orderInfo, String ipAddress,
                                   java.time.LocalDateTime createdAt, java.time.LocalDateTime expiresAt) {
        requireConfiguration();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress == null || ipAddress.isBlank() ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_ExpireDate", expiresAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        String query = canonical(params);
        return payUrl + "?" + query + "&vnp_SecureHash=" + hmacSha512(query);
    }

    public boolean verify(Map<String, String> params) {
        requireConfiguration();
        if (!tmnCode.equals(params.get("vnp_TmnCode"))) return false;
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) return false;
        Map<String, String> signed = new TreeMap<>(params);
        signed.remove("vnp_SecureHash");
        signed.remove("vnp_SecureHashType");
        String expected = hmacSha512(canonical(signed));
        return MessageDigest.isEqual(expected.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                received.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    private String canonical(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("Không thể tạo chữ ký VNPay", e); }
    }

    private void requireConfiguration() {
        if (tmnCode == null || tmnCode.isBlank() || hashSecret == null || hashSecret.isBlank())
            throw new BusinessException("Chưa cấu hình VNPay Sandbox. Hãy đặt VNPAY_TMN_CODE và VNPAY_HASH_SECRET.");
    }
}
