package com.lianpayhub.service.ai;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.repository.AiProviderConfigRepository;
import com.lianpayhub.repository.AppAiProviderSettingRepository;
import com.lianpayhub.repository.UserAiCredentialRepository;
import com.lianpayhub.service.ai.provider.AiProviderRegistry;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiGatewayService {
    private final AppAiProviderSettingRepository appAiProviderSettingRepository;
    private final UserAiCredentialRepository userAiCredentialRepository;
    private final AiProviderConfigRepository aiProviderConfigRepository;
    private final AiProviderRegistry aiProviderRegistry;
    private final AppPlatformPolicyService appPlatformPolicyService;

    public AiGatewayService(AppAiProviderSettingRepository appAiProviderSettingRepository,
                            UserAiCredentialRepository userAiCredentialRepository,
                            AiProviderConfigRepository aiProviderConfigRepository,
                            AiProviderRegistry aiProviderRegistry,
                            AppPlatformPolicyService appPlatformPolicyService) {
        this.appAiProviderSettingRepository = appAiProviderSettingRepository;
        this.userAiCredentialRepository = userAiCredentialRepository;
        this.aiProviderConfigRepository = aiProviderConfigRepository;
        this.aiProviderRegistry = aiProviderRegistry;
        this.appPlatformPolicyService = appPlatformPolicyService;
    }

    @Transactional(readOnly = true)
    public AiChatResult chat(Long userId, String appId, String providerCode, String model, String message, boolean stream, String imageUrl) {
        AppAiProviderSetting setting = resolveSetting(appId, providerCode);
        UserAiCredential credential = userAiCredentialRepository
                .findByUserIdAndAppIdAndProviderCode(userId, appId, setting.getProviderCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "用户未配置对应 AI Key"));
        AiProviderConfig provider = aiProviderConfigRepository.findByProviderCode(setting.getProviderCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI平台不存在"));
        return aiProviderRegistry.require(setting.getProviderCode()).chat(provider, credential, model, message, stream, imageUrl);
    }

    private AppAiProviderSetting resolveSetting(String appId, String providerCode) {
        AppPlatformPolicy policy = appPlatformPolicyService.find(appId, PlatformConfigCategory.AI).orElse(null);
        if (policy != null && !policy.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP AI 策略已停用");
        }
        String safeProviderCode = providerCode != null && !providerCode.trim().isEmpty()
                ? providerCode.trim()
                : policy == null ? null : policy.getProviderCode();
        if (safeProviderCode != null && !safeProviderCode.trim().isEmpty()) {
            return appAiProviderSettingRepository.findByAppIdAndProviderCode(appId, safeProviderCode.trim())
                    .filter(AppAiProviderSetting::isEnabled)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 未启用该 AI 平台"));
        }
        return appAiProviderSettingRepository.findByAppIdOrderByIdAsc(appId).stream()
                .filter(AppAiProviderSetting::isEnabled)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 未配置可用 AI 平台"));
    }
}
