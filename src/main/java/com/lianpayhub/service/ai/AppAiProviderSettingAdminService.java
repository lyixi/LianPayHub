package com.lianpayhub.service.ai;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.repository.AppAiProviderSettingRepository;
import com.lianpayhub.repository.AppInfoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppAiProviderSettingAdminService {
    private final AppAiProviderSettingRepository repository;
    private final AppInfoRepository appInfoRepository;

    public AppAiProviderSettingAdminService(AppAiProviderSettingRepository repository,
                                            AppInfoRepository appInfoRepository) {
        this.repository = repository;
        this.appInfoRepository = appInfoRepository;
    }

    @Transactional(readOnly = true)
    public List<AppAiProviderSetting> list(String appId) {
        return repository.findByAppIdOrderByIdAsc(appId);
    }

    @Transactional
    public AppAiProviderSetting upsert(String appId, String providerCode, boolean enabled,
                                       boolean autoProvisionUserKey, Long defaultQuotaUnits,
                                       Long dailyLimitUnits, String keyGroupId) {
        if (!appInfoRepository.existsByAppId(appId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        }
        AppAiProviderSetting current = repository.findByAppIdAndProviderCode(appId, providerCode).orElse(null);
        if (current == null) {
            return repository.save(new AppAiProviderSetting(appId, providerCode, enabled, autoProvisionUserKey,
                    defaultQuotaUnits, dailyLimitUnits, keyGroupId));
        }
        current.update(enabled, autoProvisionUserKey, defaultQuotaUnits, dailyLimitUnits, keyGroupId);
        return repository.save(current);
    }
}
