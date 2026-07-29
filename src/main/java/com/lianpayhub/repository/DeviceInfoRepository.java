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
    Page<DeviceInfo> findByUserId(Long userId, Pageable pageable);
    Page<DeviceInfo> findByAppIdAndDeviceCode(String appId, String deviceCode, Pageable pageable);
    List<DeviceInfo> findTop10ByAppIdOrderByIdDesc(String appId);
    List<DeviceInfo> findTop10ByUserIdOrderByIdDesc(Long userId);
    long countByAppId(String appId);
    long countByUserId(Long userId);
    void deleteByAppId(String appId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
