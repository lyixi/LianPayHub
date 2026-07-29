package com.lianpayhub.repository;

import com.lianpayhub.domain.ai.AppAiProviderSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppAiProviderSettingRepository extends JpaRepository<AppAiProviderSetting, Long> {
    List<AppAiProviderSetting> findByAppIdOrderByIdAsc(String appId);
    Optional<AppAiProviderSetting> findByAppIdAndProviderCode(String appId, String providerCode);
}
