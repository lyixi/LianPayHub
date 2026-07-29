package com.lianpayhub.repository;

import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPlatformPolicyRepository extends JpaRepository<AppPlatformPolicy, Long> {
    List<AppPlatformPolicy> findByAppIdOrderByCategoryAsc(String appId);
    Optional<AppPlatformPolicy> findByAppIdAndCategory(String appId, PlatformConfigCategory category);
}
