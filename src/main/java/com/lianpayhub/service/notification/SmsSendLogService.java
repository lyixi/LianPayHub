package com.lianpayhub.service.notification;

import com.lianpayhub.domain.notification.SmsSendLog;
import com.lianpayhub.repository.SmsSendLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SmsSendLogService {

    private final SmsSendLogRepository smsSendLogRepository;

    public SmsSendLogService(SmsSendLogRepository smsSendLogRepository) {
        this.smsSendLogRepository = smsSendLogRepository;
    }

    public SmsSendLog save(Long configId, String providerCode, String appId, String mobile,
                           String templateCode, String paramsJson, NotificationSendResult result) {
        SmsSendLog log = new SmsSendLog(
                configId,
                "SMS",
                providerCode,
                appId,
                mobile,
                templateCode,
                paramsJson,
                result == null ? null : result.getMessageId(),
                result != null && result.isSuccess(),
                result == null ? null : result.getMessage()
        );
        return smsSendLogRepository.save(log);
    }

    public Page<SmsSendLog> pageSmsLogs(Pageable pageable) {
        return smsSendLogRepository.findByChannelTypeOrderByIdDesc("SMS", pageable);
    }
}
