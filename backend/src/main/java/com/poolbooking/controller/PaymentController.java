package com.poolbooking.controller;

import com.poolbooking.dto.ApiResponse;
import com.poolbooking.dto.BookingDtos.PaymentRequest;
import com.poolbooking.dto.BookingDtos.PaymentLinkResponse;
import com.poolbooking.entity.DatLich;
import com.poolbooking.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;
    @Value("${app.vnpay.frontend-url:http://localhost:5173}") private String frontendUrl;
    public PaymentController(PaymentService payments) { this.payments = payments; }
    @PostMapping
    ApiResponse<DatLich> pay(@Valid @RequestBody PaymentRequest request, Authentication auth) {
        return ApiResponse.ok("Thanh toán mô phỏng thành công", payments.pay(auth.getName(), request));
    }

    @PostMapping("/vnpay")
    ApiResponse<PaymentLinkResponse> createVnPay(@RequestParam String maDat, Authentication auth, HttpServletRequest request) {
        String url = payments.createVnPayUrl(auth.getName(), maDat, request.getRemoteAddr());
        return ApiResponse.ok("Đã tạo liên kết thanh toán VNPay", new PaymentLinkResponse(url));
    }

    @RequestMapping(path = "/vnpay/ipn", method = {RequestMethod.GET, RequestMethod.POST})
    Map<String, String> ipn(@RequestParam Map<String, String> params) {
        return payments.handleVnPayIpn(params);
    }

    @GetMapping("/vnpay/return")
    ResponseEntity<Void> returnFromVnPay(@RequestParam Map<String, String> params) {
        boolean valid = payments.verifyVnPayReturn(params);
        String txnRef = params.get("vnp_TxnRef");
        String bookingId = valid && txnRef != null ? payments.bookingIdForVnPayReturn(txnRef) : null;
        String target = frontendUrl.replaceAll("/$", "") + "/my/bookings";
        if (bookingId != null) {
            String result = "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus")) ? "success" : "failed";
            target += "/" + bookingId + "?payment=" + result;
        } else target += "?payment=invalid";
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}
