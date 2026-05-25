package com.lianpayhub.repository;

import com.lianpayhub.domain.device.DeviceInfo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {
    Optional<DeviceInfo> findByAppIdAndDeviceCode(String appId, String deviceCode);
    Page<DeviceInfo> findByAppId(String appId, Pageable pageable);
    Page<DeviceInfo> findByAppIdAndUserId(String appId, Long userId, Pageable pageable);
    Page<DeviceInfo> findByAppIdAndDeviceCode(String appId, String deviceCode, Pageable pageable);
}
