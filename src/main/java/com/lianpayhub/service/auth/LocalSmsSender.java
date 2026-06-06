package com.lianpayhub.service.auth;

import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.service.notification.NotificationSendService;
import org.springframework.stereotype.Component;

@Component
public class LocalSmsSender implements SmsSender {

    private final SecurityProperties securityProperties;
    private final NotificationSendService notificationSendService;

    public LocalSmsSender(SecurityProperties securityProperties, NotificationSendService notificationSendService) {
        this.securityProperties = securityProperties;
        this.notificationSendService = notificationSendService;
    }

    @Override
    public void send(String appId, String mobile, String code, int expireMinutes) {
        notificationSendService.sendSmsCode(
                appId, mobile, code, expireMinutes, securityProperties.getSmsProvider());
    }
}
