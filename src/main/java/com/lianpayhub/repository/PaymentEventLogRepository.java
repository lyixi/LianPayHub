package com.lianpayhub.repository;

import com.lianpayhub.domain.log.PaymentEventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventLogRepository extends JpaRepository<PaymentEventLog, Long> {
    Page<PaymentEventLog> findByAppId(String appId, Pageable pageable);
    Page<PaymentEventLog> findByOrderId(Long orderId, Pageable pageable);
}
