package com.lianpayhub.repository;

import com.lianpayhub.domain.device.DeviceCodeChangeLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCodeChangeLogRepository extends JpaRepository<DeviceCodeChangeLog, Long> {
    Page<DeviceCodeChangeLog> findByDeviceId(Long deviceId, Pageable pageable);
    List<DeviceCodeChangeLog> findTop10ByDeviceIdOrderByIdDesc(Long deviceId);
}
