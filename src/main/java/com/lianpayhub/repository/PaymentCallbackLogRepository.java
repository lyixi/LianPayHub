package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PaymentCallbackLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {
    Page<PaymentCallbackLog> findByOrderId(Long orderId, Pageable pageable);
    Page<PaymentCallbackLog> findByAppId(String appId, Pageable pageable);
}
