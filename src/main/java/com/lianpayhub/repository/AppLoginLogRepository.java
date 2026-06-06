package com.lianpayhub.repository;

import com.lianpayhub.domain.log.AppLoginLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppLoginLogRepository extends JpaRepository<AppLoginLog, Long> {
    Page<AppLoginLog> findByAppId(String appId, Pageable pageable);
    Page<AppLoginLog> findByUserId(Long userId, Pageable pageable);
    Page<AppLoginLog> findByMobile(String mobile, Pageable pageable);
    Page<AppLoginLog> findByAppIdAndMobile(String appId, String mobile, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
}
