package com.lianpayhub.repository;

import com.lianpayhub.domain.notification.SmsSendLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsSendLogRepository extends JpaRepository<SmsSendLog, Long> {
    Page<SmsSendLog> findByChannelTypeOrderByIdDesc(String channelType, Pageable pageable);
}
