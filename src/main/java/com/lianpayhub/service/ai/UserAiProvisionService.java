package com.lianpayhub.service.ai;

import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.repository.AiProviderConfigRepository;
import com.lianpayhub.repository.AppAiProviderSettingRepository;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.UserAiCredentialRepository;
import com.lianpayhub.service.ai.provider.AiProviderRegistry;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAiProvisionService {
    private final AppInfoRepository appInfoRepository;
    private final UserAiCredentialRepository repository;
    private final AppAiProviderSettingRepository appAiProviderSettingRepository;
    private final AiProviderConfigRepository aiProviderConfigRepository;
    private final AiProviderRegistry aiProviderRegistry;
    private final AppPlatformPolicyService appPlatformPolicyService;

    public UserAiProvisionService(AppInfoRepository appInfoRepository,
                                  UserAiCredentialRepository repository,
                                  AppAiProviderSettingRepository appAiProviderSettingRepository,
                                  AiProviderConfigRepository aiProviderConfigRepository,
                                  AiProviderRegistry aiProviderRegistry,
                                  AppPlatformPolicyService appPlatformPolicyService) {
        this.appInfoRepository = appInfoRepository;
        this.repository = repository;
        this.appAiProviderSettingRepository = appAiProviderSettingRepository;
        this.aiProviderConfigRepository = aiProviderConfigRepository;
        this.aiProviderRegistry = aiProviderRegistry;
        this.appPlatformPolicyService = appPlatformPolicyService;
    }

    @Transactional
    public void ensureCredentialForBoundUser(Long userId, String appId) {
        if (userId == null || appId == null || appId.trim().isEmpty()) return;
        AppInfo app = appInfoRepository.findByAppId(appId).orElse(null);
        if (app == null) return;
        AppPlatformPolicy aiPolicy = appPlatformPolicyService.find(appId, PlatformConfigCategory.AI).orElse(null);
        if (aiPolicy != null && !aiPolicy.isEnabled()) return;
        List<AppAiProviderSetting> settings = appAiProviderSettingRepository.findByAppIdOrderByIdAsc(appId);
        if (!settings.isEmpty()) {
            for (AppAiProviderSetting setting : settings) {
                if (!setting.isEnabled() || !setting.isAutoProvisionUserKey()) continue;
                if (repository.findByUserIdAndAppIdAndProviderCode(userId, appId, setting.getProviderCode()).isPresent()) continue;
                String createdKey = null;
                AiProviderConfig providerConfig = aiProviderConfigRepository.findByProviderCode(setting.getProviderCode()).orElse(null);
                if (providerConfig != null) {
                    try {
                        createdKey = aiProviderRegistry.require(setting.getProviderCode()).createUserKey(providerConfig, setting, userId);
                    } catch (RuntimeException ignored) {
                    }
                }
                repository.save(new UserAiCredential(userId, appId, setting.getProviderCode(), createdKey, setting.getDefaultQuotaUnits()));
            }
            return;
        }
        if (!app.isEnableUserAiKey()) return;
        String providerCode = aiPolicy != null && aiPolicy.getProviderCode() != null
                ? aiPolicy.getProviderCode()
                : app.getDefaultAiProviderCode();
        if (providerCode == null || providerCode.trim().isEmpty()) return;
        if (repository.findByUserIdAndAppIdAndProviderCode(userId, appId, providerCode).isPresent()) return;
        repository.save(new UserAiCredential(userId, appId, providerCode, null, app.getDefaultAiQuotaUnits()));
    }
}
