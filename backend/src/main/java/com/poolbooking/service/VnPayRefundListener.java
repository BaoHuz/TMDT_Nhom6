package com.poolbooking.service;

import com.poolbooking.entity.ThanhToan;
import com.poolbooking.enums.TrangThaiHoanTien;
import com.poolbooking.repository.ThanhToanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;

@Component
public class VnPayRefundListener {
    private final ThanhToanRepository payments;
    private final VnPayGateway gateway;
    @Value("${server.address:127.0.0.1}") private String serverIp;

    public VnPayRefundListener(ThanhToanRepository payments, VnPayGateway gateway) {
        this.payments=payments; this.gateway=gateway;
    }

    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void onRefundRequested(RefundRequestedEvent event) {
        ThanhToan p=payments.findById(event.paymentId()).orElse(null);
        if(p==null||p.trangThaiHoan!=TrangThaiHoanTien.CHO_HOAN||p.maGiaoDich==null||p.soTienHoan==null) return;
        try {
            boolean full=p.soTienHoan.compareTo(p.soTien)==0;
            String amount=p.soTienHoan.movePointRight(2).toBigIntegerExact().toString();
            VnPayGateway.RefundResult result=gateway.refund(p.maGiaoDichHoan,p.maGiaoDich,amount,full,p.maGiaoDichVnpay,
                    p.thoiDiem.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                    "Hoan tien PoolBooking "+p.maDat,"PoolBooking",serverIp);
            setStatus(p.maTt,result.accepted()?TrangThaiHoanTien.DANG_XU_LY:
                    ("94".equals(result.responseCode())?TrangThaiHoanTien.DANG_XU_LY:TrangThaiHoanTien.HOAN_THAT_BAI));
        } catch(RuntimeException ex) {
            // Timeout is ambiguous. Keep pending so an operator can reconcile with QueryDR.
            setStatus(p.maTt,TrangThaiHoanTien.DANG_XU_LY);
        }
    }

    @Transactional
    public void setStatus(String id,TrangThaiHoanTien status) {
        payments.findById(id).ifPresent(p->{p.trangThaiHoan=status;if(status==TrangThaiHoanTien.HOAN_THANH)p.thoiGianHoan=java.time.LocalDateTime.now();payments.save(p);});
    }
}
