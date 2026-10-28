package com.poolbooking;

import com.poolbooking.service.VnPayGateway;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VnPayGatewayTest {
    @Test
    void verifiesSignedRefundResponseAndRejectsTamperedResponse() throws Exception {
        String secret = "refund-test-secret";
        VnPayGateway gateway = new VnPayGateway(RestClient.builder());
        ReflectionTestUtils.setField(gateway, "hashSecret", secret);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("vnp_ResponseId", "RESP-01");
        response.put("vnp_Command", "refund");
        response.put("vnp_ResponseCode", "00");
        response.put("vnp_Message", "Confirm Success");
        response.put("vnp_TmnCode", "TMNCODE");
        response.put("vnp_TxnRef", "VN-TXN-01");
        response.put("vnp_Amount", "25000000");
        response.put("vnp_BankCode", "NCB");
        response.put("vnp_PayDate", "20261028215000");
        response.put("vnp_TransactionNo", "123456789");
        response.put("vnp_TransactionType", "02");
        response.put("vnp_TransactionStatus", "00");
        response.put("vnp_OrderInfo", "Hoan tien don DL-01");
        response.put("vnp_SecureHash", signature(secret,
            "RESP-01|refund|00|Confirm Success|TMNCODE|VN-TXN-01|250000|NCB|20261028215000|123456789|02|00|Hoan tien don DL-01"));

        assertThat(gateway.verifyRefundResponse(response)).isTrue();

        response.put("vnp_ResponseCode", "91");
        assertThat(gateway.verifyRefundResponse(response)).isFalse();
    }

    private String signature(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
