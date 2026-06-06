package com.lianpayhub.repository;

import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelStatus;
import com.lianpayhub.domain.notification.NotificationChannelType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationChannelConfigRepository extends JpaRepository<NotificationChannelConfig, Long> {
    boolean existsByChannelTypeAndProviderCode(NotificationChannelType channelType, String providerCode);
    long countByChannelType(NotificationChannelType channelType);

    Optional<NotificationChannelConfig> findByChannelTypeAndProviderCode(NotificationChannelType channelType,
                                                                         String providerCode);

    Optional<NotificationChannelConfig> findFirstByChannelTypeAndStatusOrderByIdAsc(NotificationChannelType channelType,
                                                                                    NotificationChannelStatus status);

    @Query("select c from NotificationChannelConfig c where (:channelType is null or c.channelType = :channelType) " +
            "and (:providerCode is null or c.providerCode = :providerCode) " +
            "and (:status is null or c.status = :status)")
    Page<NotificationChannelConfig> search(@Param("channelType") NotificationChannelType channelType,
                                           @Param("providerCode") String providerCode,
                                           @Param("status") NotificationChannelStatus status,
                                           Pageable pageable);
}
