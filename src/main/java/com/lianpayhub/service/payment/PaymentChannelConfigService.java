package com.lianpayhub.service.payment;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import com.lianpayhub.domain.payment.PaymentChannelConfigStatus;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.PaymentChannelConfigRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentChannelConfigService {

    public static final String PLATFORM_APP_ID = "__PLATFORM__";

    private final PaymentChannelConfigRepository configRepository;
    private final AppInfoRepository appInfoRepository;

    public PaymentChannelConfigService(PaymentChannelConfigRepository configRepository,
                                       AppInfoRepository appInfoRepository) {
        this.configRepository = configRepository;
        this.appInfoRepository = appInfoRepository;
    }

    public Page<PaymentChannelConfig> search(String appId, PayChannel payChannel,
                                             PaymentChannelConfigStatus status, Pageable pageable) {
        return configRepository.search(normalize(appId), payChannel, status, pageable);
    }

    public PaymentChannelConfig detail(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "支付渠道配置不存在"));
    }

    @Transactional
    public PaymentChannelConfig create(String appId, PayChannel payChannel, String providerCode, String merchantId,
                                       String channelAppId, String notifyUrl, String configJson,
                                       String credentialJson) {
        String safeAppId = platformAppId(appId);
        PayChannel safePayChannel = requirePayChannel(payChannel);
        String safeProviderCode = requireText(providerCode, "providerCode 不能为空");
        if (configRepository.existsByAppIdAndPayChannel(safeAppId, safePayChannel)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该 APP 的支付渠道配置已存在");
        }
        return configRepository.save(new PaymentChannelConfig(
                safeAppId,
                safePayChannel,
                safeProviderCode,
                normalize(merchantId),
                normalize(channelAppId),
                normalize(notifyUrl),
                normalize(configJson),
                normalize(credentialJson)
        ));
    }

    @Transactional
    public PaymentChannelConfig update(Long id, String providerCode, String merchantId, String channelAppId,
                                       String notifyUrl, String configJson, String credentialJson) {
        PaymentChannelConfig config = detail(id);
        config.update(
                requireText(providerCode, "providerCode 不能为空"),
                normalize(merchantId),
                normalize(channelAppId),
                normalize(notifyUrl),
                normalize(configJson),
                normalize(credentialJson)
        );
        return configRepository.save(config);
    }

    @Transactional
    public PaymentChannelConfig changeStatus(Long id, PaymentChannelConfigStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status 不能为空");
        }
        PaymentChannelConfig config = detail(id);
        config.changeStatus(status);
        return configRepository.save(config);
    }

    public Optional<PaymentChannelConfig> findByAppAndChannel(String appId, PayChannel payChannel) {
        Optional<PaymentChannelConfig> appConfig = configRepository.findByAppIdAndPayChannel(appId, payChannel);
        if (appConfig.isPresent()) {
            return appConfig;
        }
        return configRepository.findByAppIdAndPayChannel(PLATFORM_APP_ID, payChannel);
    }

    public java.util.List<PayChannel> listEnabledChannels() {
        java.util.List<PayChannel> channels = new java.util.ArrayList<PayChannel>();
        for (PaymentChannelConfig config : configRepository.findByAppIdAndStatus(
                PLATFORM_APP_ID, PaymentChannelConfigStatus.ENABLED)) {
            channels.add(config.getPayChannel());
        }
        return channels;
    }

    private String requireAppId(String appId) {
        String safeAppId = requireText(appId, "appId 不能为空");
        if (!appInfoRepository.existsByAppId(safeAppId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        }
        return safeAppId;
    }

    private String platformAppId(String appId) {
        String safeAppId = normalize(appId);
        if (safeAppId == null || PLATFORM_APP_ID.equals(safeAppId)) {
            return PLATFORM_APP_ID;
        }
        return requireAppId(safeAppId);
    }

    private PayChannel requirePayChannel(PayChannel payChannel) {
        if (payChannel == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "payChannel 不能为空");
        }
        return payChannel;
    }

    private String requireText(String value, String message) {
        String safeValue = normalize(value);
        if (safeValue == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return safeValue;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
