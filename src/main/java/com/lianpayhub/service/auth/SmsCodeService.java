package com.lianpayhub.service.auth;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppType;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import com.lianpayhub.service.rate.RateLimitService;
import com.fasterxml.jackson.databind.JsonNode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeService {

    private final Map<String, SmsCodeEntry> codes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final AppService appService;
    private final RateLimitService rateLimitService;
    private final SecurityProperties securityProperties;
    private final SmsSender smsSender;
    private final AppPlatformPolicyService appPlatformPolicyService;

    public SmsCodeService(AppService appService, RateLimitService rateLimitService,
                          SecurityProperties securityProperties, SmsSender smsSender,
                          AppPlatformPolicyService appPlatformPolicyService) {
        this.appService = appService;
        this.rateLimitService = rateLimitService;
        this.securityProperties = securityProperties;
        this.smsSender = smsSender;
        this.appPlatformPolicyService = appPlatformPolicyService;
    }

    public SendSmsCodeResult sendCode(String appId, String mobile, String ipAddress) {
        AppInfo appInfo = appService.requireEnabledApp(appId);
        requireMobileLoginSupported(appInfo);
        AppPlatformPolicy smsPolicy = requireEnabledPolicy(appId, PlatformConfigCategory.SMS, "APP 短信策略已停用");
        AppPlatformPolicy captchaPolicy = requireEnabledPolicy(appId, PlatformConfigCategory.CAPTCHA, "APP 验证码策略已停用");
        JsonNode smsPolicyJson = appPlatformPolicyService.policyJson(smsPolicy);
        JsonNode captchaPolicyJson = appPlatformPolicyService.policyJson(captchaPolicy);
        cleanupExpiredCodes();

        Instant now = Instant.now();
        String key = key(appId, mobile);
        SmsCodeEntry current = codes.get(key);
        int cooldownSeconds = positiveOrDefault(
                appPlatformPolicyService.intValue(smsPolicyJson, "cooldownSeconds",
                        securityProperties.getSmsCodeCooldownSeconds() == null ? 60 : securityProperties.getSmsCodeCooldownSeconds()),
                60);
        if (current != null && current.lastSentAt.plusSeconds(cooldownSeconds).isAfter(now)) {
            throw new BusinessException(ErrorCode.RATE_LIMITED,
                    "验证码发送过于频繁，请稍后再试");
        }

        rateLimitService.requireWithinLimit("sms:mobile:" + key, 5, Duration.ofMinutes(10));
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            rateLimitService.requireWithinLimit("sms:ip:" + ipAddress.trim(), 30, Duration.ofMinutes(10));
        }

        int expireMinutes = positiveOrDefault(
                appPlatformPolicyService.intValue(captchaPolicyJson, "expireMinutes",
                        appPlatformPolicyService.intValue(smsPolicyJson, "expireMinutes",
                                securityProperties.getSmsCodeExpireMinutes() == null ? 5 : securityProperties.getSmsCodeExpireMinutes())),
                5);
        String code = generateCode();
        codes.put(key, new SmsCodeEntry(code, now.plus(Duration.ofMinutes(expireMinutes)), now));
        smsSender.send(appId, mobile, code, expireMinutes);
        return new SendSmsCodeResult(true, expireMinutes * 60,
                Boolean.TRUE.equals(securityProperties.getSmsDebugReturnCode()) ? code : null);
    }

    public void verifyAndConsume(String appId, String mobile, String code) {
        if (isBlank(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }

        String key = key(appId, mobile);
        SmsCodeEntry entry = codes.get(key);
        Instant now = Instant.now();
        if (entry == null || !entry.expireAt.isAfter(now)) {
            codes.remove(key);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不存在或已过期");
        }

        AppPlatformPolicy captchaPolicy = appPlatformPolicyService.find(appId, PlatformConfigCategory.CAPTCHA).orElse(null);
        JsonNode captchaPolicyJson = appPlatformPolicyService.policyJson(captchaPolicy);
        int maxAttempts = positiveOrDefault(
                appPlatformPolicyService.intValue(captchaPolicyJson, "maxAttempts",
                        securityProperties.getSmsCodeMaxAttempts() == null ? 5 : securityProperties.getSmsCodeMaxAttempts()),
                5);
        if (entry.attempts >= maxAttempts) {
            codes.remove(key);
            throw new BusinessException(ErrorCode.RATE_LIMITED, "验证码错误次数过多，请重新获取");
        }

        if (!entry.code.equals(code.trim())) {
            entry.attempts++;
            if (entry.attempts >= maxAttempts) {
                codes.remove(key);
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不正确");
        }

        codes.remove(key);
    }

    private void requireMobileLoginSupported(AppInfo appInfo) {
        if (appInfo.getAppType() == AppType.DEVICE_ONLY || !appInfo.isNeedMobileLogin()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 不支持手机号登录");
        }
    }

    private void cleanupExpiredCodes() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, SmsCodeEntry>> iterator = codes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SmsCodeEntry> item = iterator.next();
            if (!item.getValue().expireAt.isAfter(now)) {
                iterator.remove();
            }
        }
    }

    private String generateCode() {
        int value = random.nextInt(1000000);
        return String.format("%06d", value);
    }

    private String key(String appId, String mobile) {
        return appId.trim() + ":" + mobile.trim();
    }

    private int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private AppPlatformPolicy requireEnabledPolicy(String appId, PlatformConfigCategory category, String disabledMessage) {
        AppPlatformPolicy policy = appPlatformPolicyService.find(appId, category).orElse(null);
        if (policy != null && !policy.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, disabledMessage);
        }
        return policy;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class SmsCodeEntry {
        private final String code;
        private final Instant expireAt;
        private final Instant lastSentAt;
        private int attempts;

        private SmsCodeEntry(String code, Instant expireAt, Instant lastSentAt) {
            this.code = code;
            this.expireAt = expireAt;
            this.lastSentAt = lastSentAt;
        }
    }
}
