package com.lianpayhub.repository;

import com.lianpayhub.domain.adapter.AdapterReport;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterReportRepository extends JpaRepository<AdapterReport, Long> {
    Page<AdapterReport> findByAppId(String appId, Pageable pageable);
    Page<AdapterReport> findByAppIdAndSourceId(String appId, String sourceId, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
}
