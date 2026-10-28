package com.poolbooking;

import com.poolbooking.service.VnPayGateway;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Opt-in sandbox test. It submits a real refund request, so provide a valid
 * successful sandbox payment reference before enabling it.
 *
 * Required environment: VNPAY_REFUND_TEST_ENABLED, VNPAY_TMN_CODE,
 * VNPAY_HASH_SECRET, VNPAY_TEST_TXN_REF, VNPAY_TEST_AMOUNT_MINOR,
 * VNPAY_TEST_PAYMENT_DATE (yyyyMMddHHmmss). Optional VNPAY_TEST_TRANSACTION_NO.
 */
class VnPayRefundSandboxIT {
    @Test
    void sendsSignedRefundRequestToVnPaySandbox() {
        assumeTrue("true".equalsIgnoreCase(System.getenv("VNPAY_REFUND_TEST_ENABLED")),
                "Set VNPAY_REFUND_TEST_ENABLED=true to submit a real sandbox refund");
        String txnRef=required("VNPAY_TEST_TXN_REF");
        String amount=required("VNPAY_TEST_AMOUNT_MINOR");
        String paymentDate=required("VNPAY_TEST_PAYMENT_DATE");
        VnPayGateway gateway=new VnPayGateway();
        ReflectionTestUtils.setField(gateway,"tmnCode",required("VNPAY_TMN_CODE"));
        ReflectionTestUtils.setField(gateway,"hashSecret",required("VNPAY_HASH_SECRET"));
        String url=System.getenv().getOrDefault("VNPAY_API_URL","https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
        ReflectionTestUtils.setField(gateway,"apiUrl",url);

        boolean fullRefund=!"false".equalsIgnoreCase(System.getenv("VNPAY_TEST_FULL_REFUND"));
        VnPayGateway.RefundResult result=gateway.refund(UUID.randomUUID().toString().replace("-",""),
                txnRef,amount,fullRefund,System.getenv("VNPAY_TEST_TRANSACTION_NO"),paymentDate,
                "Sandbox refund test","PoolBooking-test","127.0.0.1");
        assertThat(result.accepted()).as("VNPay accepted refund request (not necessarily credited to card yet)").isTrue();
    }

    private String required(String key) {
        String value=System.getenv(key);
        assumeTrue(value!=null&&!value.isBlank(),"Missing environment variable "+key);
        return value;
    }
}
