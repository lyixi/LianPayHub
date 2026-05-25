package com.lianpayhub.repository;

import com.lianpayhub.domain.app.AppInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppInfoRepository extends JpaRepository<AppInfo, Long> {
    Optional<AppInfo> findByAppId(String appId);
    boolean existsByAppId(String appId);
}
