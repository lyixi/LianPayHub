package com.lianpayhub.repository;

import com.lianpayhub.domain.launch.LaunchRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LaunchRecordRepository extends JpaRepository<LaunchRecord, Long> {
    Page<LaunchRecord> findByAppId(String appId, Pageable pageable);
    Page<LaunchRecord> findByAppIdAndDeviceId(String appId, Long deviceId, Pageable pageable);
    Page<LaunchRecord> findByAppIdAndUserId(String appId, Long userId, Pageable pageable);
    List<LaunchRecord> findTop10ByAppIdOrderByIdDesc(String appId);
    List<LaunchRecord> findTop10ByDeviceIdOrderByIdDesc(Long deviceId);
    Optional<LaunchRecord> findFirstByAppIdAndDeviceIdAndSessionIdAndEventTypeOrderByIdDesc(
            String appId, Long deviceId, String sessionId, com.lianpayhub.domain.launch.LaunchEventType eventType);
    long countByAppId(String appId);
    long countByDeviceId(Long deviceId);

    @Query("select coalesce(sum(r.durationSeconds), 0) from LaunchRecord r where r.deviceId = ?1")
    Long sumDurationSecondsByDeviceId(Long deviceId);

    @Query("select coalesce(avg(r.durationSeconds), 0) from LaunchRecord r where r.deviceId = ?1 and r.durationSeconds is not null")
    Double avgDurationSecondsByDeviceId(Long deviceId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
}
