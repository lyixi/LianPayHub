package com.lianpayhub.repository;

import com.lianpayhub.domain.config.UserConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {
    List<UserConfig> findByUserIdAndAppIdOrderByConfigKeyAsc(Long userId, String appId);
    List<UserConfig> findByUserIdAndAppIdAndVersionGreaterThanOrderByVersionAsc(Long userId, String appId, Long version);
    Optional<UserConfig> findByUserIdAndAppIdAndConfigKey(Long userId, String appId, String configKey);
    void deleteByUserIdAndAppIdAndConfigKey(Long userId, String appId, String configKey);
}
