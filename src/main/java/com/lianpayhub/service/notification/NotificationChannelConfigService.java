package com.lianpayhub.service.notification;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelStatus;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.repository.NotificationChannelConfigRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationChannelConfigService {

    private final NotificationChannelConfigRepository configRepository;

    public NotificationChannelConfigService(NotificationChannelConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Page<NotificationChannelConfig> search(NotificationChannelType channelType, String providerCode,
                                                  NotificationChannelStatus status, Pageable pageable) {
        return configRepository.search(channelType, normalizeProvider(providerCode), status, pageable);
    }

    public NotificationChannelConfig detail(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "通知通道配置不存在"));
    }

    @Transactional
    public NotificationChannelConfig create(NotificationChannelType channelType, String providerCode,
                                            String displayName, String senderName, String senderAddress,
                                            String endpoint, String templateCode, String accessKeyId,
                                            String accessKeySecret, String secretId, String secretKey,
                                            String sdkAppId, String region, String configJson,
                                            String credentialJson) {
        NotificationChannelType safeType = requireChannelType(channelType);
        String safeProvider = requireText(normalizeProvider(providerCode), "providerCode 不能为空");
        String safeDisplayName = requireText(displayName, "displayName 不能为空");
        if (configRepository.existsByChannelTypeAndProviderCode(safeType, safeProvider)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该通知通道配置已存在");
        }
        return configRepository.save(new NotificationChannelConfig(
                safeType,
                safeProvider,
                safeDisplayName,
                normalize(senderName),
                normalize(senderAddress),
                normalize(endpoint),
                normalize(templateCode),
                normalize(accessKeyId),
                normalize(accessKeySecret),
                normalize(secretId),
                normalize(secretKey),
                normalize(sdkAppId),
                normalize(region),
                normalize(configJson),
                normalize(credentialJson)
        ));
    }

    @Transactional
    public NotificationChannelConfig update(Long id, String providerCode, String displayName,
                                            String senderName, String senderAddress, String endpoint,
                                            String templateCode, String accessKeyId, String accessKeySecret,
                                            String secretId, String secretKey, String sdkAppId, String region,
                                            String configJson, String credentialJson) {
        NotificationChannelConfig config = detail(id);
        String safeProvider = requireText(normalizeProvider(providerCode), "providerCode 不能为空");
        String safeDisplayName = requireText(displayName, "displayName 不能为空");
        Optional<NotificationChannelConfig> duplicate =
                configRepository.findByChannelTypeAndProviderCode(config.getChannelType(), safeProvider);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该通知通道配置已存在");
        }
        config.update(
                safeProvider,
                safeDisplayName,
                normalize(senderName),
                normalize(senderAddress),
                normalize(endpoint),
                normalize(templateCode),
                normalize(accessKeyId),
                normalize(accessKeySecret),
                normalize(secretId),
                normalize(secretKey),
                normalize(sdkAppId),
                normalize(region),
                normalize(configJson),
                normalize(credentialJson)
        );
        return configRepository.save(config);
    }

    @Transactional
    public NotificationChannelConfig changeStatus(Long id, NotificationChannelStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status 不能为空");
        }
        NotificationChannelConfig config = detail(id);
        config.changeStatus(status);
        return configRepository.save(config);
    }

    @Transactional
    public void delete(Long id) {
        configRepository.delete(detail(id));
    }

    public Optional<NotificationChannelConfig> findEnabled(NotificationChannelType channelType, String providerCode) {
        NotificationChannelType safeType = requireChannelType(channelType);
        String safeProvider = normalizeProvider(providerCode);
        if (safeProvider != null) {
            Optional<NotificationChannelConfig> preferred =
                    configRepository.findByChannelTypeAndProviderCode(safeType, safeProvider);
            if (preferred.isPresent() && preferred.get().isEnabled()) {
                return preferred;
            }
        }
        return configRepository.findFirstByChannelTypeAndStatusOrderByIdAsc(
                safeType, NotificationChannelStatus.ENABLED);
    }

    public boolean hasAny(NotificationChannelType channelType) {
        return configRepository.countByChannelType(requireChannelType(channelType)) > 0;
    }

    private NotificationChannelType requireChannelType(NotificationChannelType channelType) {
        if (channelType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "channelType 不能为空");
        }
        return channelType;
    }

    private String requireText(String value, String message) {
        String safeValue = normalize(value);
        if (safeValue == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return safeValue;
    }

    private String normalizeProvider(String value) {
        String safeValue = normalize(value);
        return safeValue == null ? null : safeValue.toLowerCase();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
