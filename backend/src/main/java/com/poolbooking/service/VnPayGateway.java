package com.poolbooking.service;

import com.poolbooking.exception.BusinessException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
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
    private final RestClient restClient;

    public VnPayGateway(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Value("${vnpay.vnp_PayUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") private String payUrl;
    @Value("${vnpay.vnp_ApiUrl:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}") private String apiUrl;
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

    public Map<String, Object> refund(RefundCommand refund) {
        requireConfiguration();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", refund.requestId());
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_TransactionType", refund.fullRefund() ? "02" : "03");
        params.put("vnp_TxnRef", refund.transactionReference());
        params.put("vnp_Amount", refund.amount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_TransactionNo", refund.transactionNumber());
        params.put("vnp_TransactionDate", refund.transactionDate());
        params.put("vnp_CreateBy", refund.createdBy());
        params.put("vnp_CreateDate", refund.createdAt().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_IpAddr", refund.ipAddress() == null || refund.ipAddress().isBlank()
            ? "127.0.0.1" : refund.ipAddress());
        params.put("vnp_OrderInfo", refund.orderInfo());
        String signatureData = String.join("|",
            params.get("vnp_RequestId"),
            params.get("vnp_Version"),
            params.get("vnp_Command"),
            params.get("vnp_TmnCode"),
            params.get("vnp_TransactionType"),
            params.get("vnp_TxnRef"),
            params.get("vnp_Amount"),
            params.get("vnp_TransactionNo"),
            params.get("vnp_TransactionDate"),
            params.get("vnp_CreateBy"),
            params.get("vnp_CreateDate"),
            params.get("vnp_IpAddr"),
            params.get("vnp_OrderInfo"));
        params.put("vnp_SecureHash", hmacSha512(signatureData));
        return restClient.post()
            .uri(apiUrl)
            .body(params)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public void validateConfiguration() {
        requireConfiguration();
    }

    public boolean verifyRefundResponse(Map<String, Object> response) {
        if (response == null) return false;
        String received = value(response, "vnp_SecureHash");
        if (received == null || received.isBlank()) return false;
        String amount = value(response, "vnp_Amount");
        if (amount != null && !amount.isBlank()) {
            try {
                amount = new BigDecimal(amount).movePointLeft(2).stripTrailingZeros().toPlainString();
            } catch (NumberFormatException ex) {
                return false;
            }
        } else {
            amount = "";
        }
        String responseData = String.join("|",
            valueOrEmpty(response, "vnp_ResponseId"),
            valueOrEmpty(response, "vnp_Command"),
            valueOrEmpty(response, "vnp_ResponseCode"),
            valueOrEmpty(response, "vnp_Message"),
            valueOrEmpty(response, "vnp_TmnCode"),
            valueOrEmpty(response, "vnp_TxnRef"),
            amount,
            valueOrEmpty(response, "vnp_BankCode"),
            valueOrEmpty(response, "vnp_PayDate"),
            valueOrEmpty(response, "vnp_TransactionNo"),
            valueOrEmpty(response, "vnp_TransactionType"),
            valueOrEmpty(response, "vnp_TransactionStatus"),
            valueOrEmpty(response, "vnp_OrderInfo"));
        return MessageDigest.isEqual(hmacSha512(responseData).toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII),
            received.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    private String canonical(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private String value(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    private String valueOrEmpty(Map<String, Object> values, String key) {
        String value = value(values, key);
        return value == null ? "" : value;
    }

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

    public record RefundCommand(String requestId, String transactionReference, BigDecimal amount,
                                String transactionNumber, String transactionDate, boolean fullRefund,
                                String createdBy, String ipAddress, String orderInfo,
                                java.time.LocalDateTime createdAt) {}
}
