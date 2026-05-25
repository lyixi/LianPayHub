package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.payment.PayStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderNo(String orderNo);
    Page<PaymentOrder> findByAppId(String appId, Pageable pageable);
    List<PaymentOrder> findTop100ByPayStatusAndExpireAtBeforeOrderByExpireAtAsc(PayStatus payStatus, LocalDateTime expireAt);
    long countByPayStatus(PayStatus payStatus);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.payStatus = ?1")
    Long sumAmountCentsByPayStatus(PayStatus payStatus);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByPayStatusAndPaidAtBetween(PayStatus payStatus, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.payStatus = ?1 and o.paidAt >= ?2 and o.paidAt < ?3")
    Long sumAmountCentsByPayStatusAndPaidAtBetween(PayStatus payStatus, LocalDateTime start, LocalDateTime end);
}
