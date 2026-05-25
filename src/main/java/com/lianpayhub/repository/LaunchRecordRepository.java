package com.lianpayhub.repository;

import com.lianpayhub.domain.launch.LaunchRecord;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaunchRecordRepository extends JpaRepository<LaunchRecord, Long> {
    Page<LaunchRecord> findByAppId(String appId, Pageable pageable);
    Page<LaunchRecord> findByAppIdAndDeviceId(String appId, Long deviceId, Pageable pageable);
    Page<LaunchRecord> findByAppIdAndUserId(String appId, Long userId, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
