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
        String provider = normalizeProvider(securityProperties.getSmsProvider());
        if (!isLogOnlyProvider(provider)) {
            throw new IllegalStateException("短信 provider 尚未接入: " + provider);
        }
        // 真实短信 SDK 接入前先统一走日志型发送器，避免本地和测试环境被外部服务卡住。
        log.info("{} sms code generated, appId={}, mobile={}, expireMinutes={}",
                displayProvider(provider), appId, maskMobile(mobile), expireMinutes);
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return "aliyun";
        }
        return provider.trim().toLowerCase();
    }

    private boolean isLogOnlyProvider(String provider) {
        return "local".equals(provider)
                || "aliyun".equals(provider)
                || "tencent".equals(provider)
                || "aggregate".equals(provider);
    }

    private String displayProvider(String provider) {
        if ("aliyun".equals(provider)) {
            return "aliyun placeholder";
        }
        if ("tencent".equals(provider)) {
            return "tencent placeholder";
        }
        if ("aggregate".equals(provider)) {
            return "aggregate placeholder";
        }
        return "local";
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "******";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
