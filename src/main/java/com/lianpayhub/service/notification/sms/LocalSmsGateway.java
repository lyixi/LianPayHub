package com.lianpayhub.service.notification.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.NotificationSendResult;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalSmsGateway extends AbstractSmsGateway {

    private static final Logger log = LoggerFactory.getLogger(LocalSmsGateway.class);

    public LocalSmsGateway(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(String providerCode) {
        return "local".equals(providerCode);
    }

    @Override
    public NotificationSendResult send(NotificationChannelConfig config, SmsSendPayload payload) {
        String messageId = "sms-log-" + UUID.randomUUID().toString();
        log.info("local sms sent to log, appId={}, mobile={}, templateCode={}, paramsJson={}, messageId={}",
                payload.getAppId(), maskMobile(payload.getMobile()), payload.getTemplateCode(),
                payload.getParamsJson(), messageId);
        return new NotificationSendResult(true, NotificationChannelType.SMS, "local", messageId,
                "本地短信已写入日志");
    }
}
