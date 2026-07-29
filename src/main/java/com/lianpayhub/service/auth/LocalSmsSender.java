package com.lianpayhub.service.auth;

import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.service.notification.NotificationSendService;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import org.springframework.stereotype.Component;

@Component
public class LocalSmsSender implements SmsSender {

    private final SecurityProperties securityProperties;
    private final NotificationSendService notificationSendService;
    private final AppPlatformPolicyService appPlatformPolicyService;

    public LocalSmsSender(SecurityProperties securityProperties, NotificationSendService notificationSendService,
                          AppPlatformPolicyService appPlatformPolicyService) {
        this.securityProperties = securityProperties;
        this.notificationSendService = notificationSendService;
        this.appPlatformPolicyService = appPlatformPolicyService;
    }

    @Override
    public void send(String appId, String mobile, String code, int expireMinutes) {
        AppPlatformPolicy policy = appPlatformPolicyService.find(appId, PlatformConfigCategory.SMS).orElse(null);
        String provider = policy != null && policy.getProviderCode() != null
                ? policy.getProviderCode()
                : securityProperties.getSmsProvider();
        notificationSendService.sendSmsCode(
                appId, mobile, code, expireMinutes, provider);
    }
}
