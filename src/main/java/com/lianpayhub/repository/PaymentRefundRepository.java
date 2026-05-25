package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.domain.payment.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    Optional<PaymentRefund> findByRefundNo(String refundNo);
    Page<PaymentRefund> findByAppId(String appId, Pageable pageable);
    Page<PaymentRefund> findByOrderId(Long orderId, Pageable pageable);

    @Query("select coalesce(sum(r.amountCents), 0) from PaymentRefund r where r.orderId = ?1 and r.status = ?2")
    Long sumAmountCentsByOrderIdAndStatus(Long orderId, RefundStatus status);
}
