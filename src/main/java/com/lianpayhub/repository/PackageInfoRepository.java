package com.lianpayhub.repository;

import com.lianpayhub.domain.packageinfo.PackageInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {
    List<PackageInfo> findByAppId(String appId);
}
