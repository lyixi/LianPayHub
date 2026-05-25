package com.lianpayhub.service.auth;

import com.lianpayhub.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LocalSmsSender.class);

    private final SecurityProperties securityProperties;

    public LocalSmsSender(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void send(String appId, String mobile, String code, int expireMinutes) {
        String provider = securityProperties.getSmsProvider();
        if (provider != null && !"local".equalsIgnoreCase(provider.trim())) {
            throw new IllegalStateException("短信 provider 尚未接入: " + provider);
        }
        log.info("local sms code generated, appId={}, mobile={}, expireMinutes={}", appId, maskMobile(mobile), expireMinutes);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "******";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
