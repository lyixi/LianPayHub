package com.lianpayhub.repository;

import com.lianpayhub.domain.device.DeviceInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {
    Optional<DeviceInfo> findByAppIdAndDeviceCode(String appId, String deviceCode);
    Page<DeviceInfo> findByAppId(String appId, Pageable pageable);
    Page<DeviceInfo> findByAppIdAndUserId(String appId, Long userId, Pageable pageable);
    Page<DeviceInfo> findByAppIdAndDeviceCode(String appId, String deviceCode, Pageable pageable);
    List<DeviceInfo> findTop10ByAppIdOrderByIdDesc(String appId);
    long countByAppId(String appId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
}
