package com.lianpayhub.repository;

import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.packageinfo.PackageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {
    List<PackageInfo> findByAppId(String appId);
    List<PackageInfo> findByAppIdAndStatus(String appId, PackageStatus status);
    long countByAppId(String appId);
}
