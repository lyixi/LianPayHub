package com.lianpayhub.service.notification.sms;

import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.service.notification.NotificationSendResult;

public interface SmsGateway {
    boolean supports(String providerCode);
    NotificationSendResult send(NotificationChannelConfig config, SmsSendPayload payload);
}
