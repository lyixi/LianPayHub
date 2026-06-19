package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.domain.payment.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    Optional<PaymentRefund> findByRefundNo(String refundNo);
    Page<PaymentRefund> findByAppId(String appId, Pageable pageable);
    Page<PaymentRefund> findByOrderId(Long orderId, Pageable pageable);
    List<PaymentRefund> findTop10ByAppIdOrderByIdDesc(String appId);

    @Query("select coalesce(sum(r.amountCents), 0) from PaymentRefund r where r.orderId = ?1 and r.status = ?2")
    Long sumAmountCentsByOrderIdAndStatus(Long orderId, RefundStatus status);

    long countByStatusAndProcessedAtBetween(RefundStatus status, LocalDateTime start, LocalDateTime end);
    long countByAppIdAndStatusAndProcessedAtBetween(String appId, RefundStatus status, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(r.amountCents), 0) from PaymentRefund r where r.status = ?1 and r.processedAt >= ?2 and r.processedAt < ?3")
    Long sumAmountCentsByStatusAndProcessedAtBetween(RefundStatus status, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(r.amountCents), 0) from PaymentRefund r where r.appId = ?1 and r.status = ?2 and r.processedAt >= ?3 and r.processedAt < ?4")
    Long sumAmountCentsByAppIdAndStatusAndProcessedAtBetween(String appId, RefundStatus status, LocalDateTime start, LocalDateTime end);
}
