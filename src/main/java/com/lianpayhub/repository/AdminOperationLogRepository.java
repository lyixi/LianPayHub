package com.lianpayhub.repository;

import com.lianpayhub.domain.log.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
    Page<AdminOperationLog> findByAdminId(Long adminId, Pageable pageable);
}
