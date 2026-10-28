package com.poolbooking.service;

import com.poolbooking.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    @Value("${vnpay.vnp_ApiUrl:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}") private String apiUrl;
    private final RestClient http = RestClient.create();

    public record RefundResult(boolean accepted, String responseCode, String transactionStatus, String transactionNo) {}

    public RefundResult refund(String requestId, String txnRef, String amount, boolean fullRefund, String transactionNo,
                               String transactionDate, String orderInfo, String createdBy, String ipAddress) {
        requireConfiguration();
        String createDate=java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String ip=ipAddress==null||ipAddress.isBlank()?"127.0.0.1":ipAddress;
        Map<String,Object> body=new LinkedHashMap<>();
        body.put("vnp_RequestId",requestId); body.put("vnp_Version","2.1.0"); body.put("vnp_Command","refund");
        body.put("vnp_TmnCode",tmnCode); body.put("vnp_TransactionType",fullRefund?"02":"03");
        body.put("vnp_TxnRef",txnRef); body.put("vnp_Amount",amount);
        if(transactionNo!=null&&!transactionNo.isBlank()) body.put("vnp_TransactionNo",transactionNo);
        body.put("vnp_TransactionDate",transactionDate); body.put("vnp_CreateBy",createdBy); body.put("vnp_CreateDate",createDate);
        body.put("vnp_IpAddr",ip); body.put("vnp_OrderInfo",orderInfo);
        String signed=String.join("|",requestId,"2.1.0","refund",tmnCode,fullRefund?"02":"03",txnRef,amount,
                transactionNo==null?"":transactionNo,transactionDate,createdBy,createDate,ip,orderInfo);
        body.put("vnp_SecureHash",hmacSha512(signed));
        Map response=http.post().uri(apiUrl).contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
        if(response==null) throw new BusinessException("VNPay không trả kết quả hoàn tiền");
        String code=String.valueOf(response.getOrDefault("vnp_ResponseCode",""));
        String status=String.valueOf(response.getOrDefault("vnp_TransactionStatus",""));
        String responseSignature=String.valueOf(response.getOrDefault("vnp_SecureHash",""));
        String responseData=String.join("|",String.valueOf(response.getOrDefault("vnp_ResponseId","")),
                String.valueOf(response.getOrDefault("vnp_Command","refund")),code,
                String.valueOf(response.getOrDefault("vnp_Message","")),tmnCode,txnRef,amount,
                String.valueOf(response.getOrDefault("vnp_BankCode","")),
                String.valueOf(response.getOrDefault("vnp_PayDate","")),
                String.valueOf(response.getOrDefault("vnp_TransactionNo","")),
                String.valueOf(response.getOrDefault("vnp_TransactionType","")),
                status,String.valueOf(response.getOrDefault("vnp_OrderInfo",orderInfo)));
        if(responseSignature.isBlank()||!MessageDigest.isEqual(hmacSha512(responseData).toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),responseSignature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII)))
            throw new BusinessException("Chữ ký phản hồi hoàn tiền VNPay không hợp lệ");
        return new RefundResult("00".equals(code),code,status,String.valueOf(response.getOrDefault("vnp_TransactionNo","")));
    }

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
