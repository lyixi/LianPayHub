package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import com.lianpayhub.domain.payment.PaymentChannelConfigStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentChannelConfigRepository extends JpaRepository<PaymentChannelConfig, Long> {
    boolean existsByAppIdAndPayChannel(String appId, PayChannel payChannel);
    Optional<PaymentChannelConfig> findByAppIdAndPayChannel(String appId, PayChannel payChannel);
    List<PaymentChannelConfig> findByAppIdAndStatus(String appId, PaymentChannelConfigStatus status);

    @Query("select c from PaymentChannelConfig c where (:appId is null or c.appId = :appId) " +
            "and (:payChannel is null or c.payChannel = :payChannel) " +
            "and (:status is null or c.status = :status)")
    Page<PaymentChannelConfig> search(@Param("appId") String appId,
                                      @Param("payChannel") PayChannel payChannel,
                                      @Param("status") PaymentChannelConfigStatus status,
                                      Pageable pageable);
}
