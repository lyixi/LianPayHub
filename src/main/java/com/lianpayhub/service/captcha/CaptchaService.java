package com.lianpayhub.service.captcha;

import com.fasterxml.jackson.databind.JsonNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import com.lianpayhub.service.rate.RateLimitService;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private static final String DEFAULT_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private final Map<String, CaptchaEntry> challenges = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final AppService appService;
    private final AppPlatformPolicyService appPlatformPolicyService;
    private final RateLimitService rateLimitService;

    public CaptchaService(AppService appService, AppPlatformPolicyService appPlatformPolicyService,
                          RateLimitService rateLimitService) {
        this.appService = appService;
        this.appPlatformPolicyService = appPlatformPolicyService;
        this.rateLimitService = rateLimitService;
    }

    public CaptchaChallengeResult createChallenge(String appId, String purpose, String ipAddress) {
        appService.requireEnabledApp(appId);
        AppPlatformPolicy policy = appPlatformPolicyService.find(appId, PlatformConfigCategory.CAPTCHA).orElse(null);
        if (policy != null && !policy.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP 验证码策略已停用");
        }
        JsonNode policyJson = appPlatformPolicyService.policyJson(policy);
        int ttlSeconds = positiveOrDefault(appPlatformPolicyService.intValue(policyJson, "ttlSeconds", 300), 300);
        int length = clamp(appPlatformPolicyService.intValue(policyJson, "length", 6), 4, 8);
        int maxAttempts = clamp(appPlatformPolicyService.intValue(policyJson, "maxAttempts", 5), 1, 20);
        boolean debugReturnCode = appPlatformPolicyService.booleanValue(policyJson, "debugReturnCode", false);
        String alphabet = appPlatformPolicyService.text(policyJson, "alphabet");
        if (alphabet == null || alphabet.length() < 4) {
            alphabet = DEFAULT_ALPHABET;
        }

        rateLimitService.requireWithinLimit("captcha:create:" + appId + ":" + safePurpose(purpose), 60, Duration.ofMinutes(1));
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            rateLimitService.requireWithinLimit("captcha:ip:" + ipAddress.trim(), 120, Duration.ofMinutes(1));
        }

        cleanupExpired();
        String token = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode(alphabet, length);
        challenges.put(token, new CaptchaEntry(appId.trim(), safePurpose(purpose), code,
                Instant.now().plusSeconds(ttlSeconds), maxAttempts));
        return new CaptchaChallengeResult(token, ttlSeconds, debugReturnCode ? code : null);
    }

    public void verifyAndConsume(String appId, String purpose, String token, String code) {
        if (isBlank(token) || isBlank(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码 token 和 code 不能为空");
        }
        CaptchaEntry entry = challenges.get(token.trim());
        if (entry == null || !entry.expireAt.isAfter(Instant.now())) {
            challenges.remove(token.trim());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不存在或已过期");
        }
        if (!entry.appId.equals(appId.trim()) || !entry.purpose.equals(safePurpose(purpose))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "验证码用途不匹配");
        }
        if (entry.attempts >= entry.maxAttempts) {
            challenges.remove(token.trim());
            throw new BusinessException(ErrorCode.RATE_LIMITED, "验证码错误次数过多");
        }
        if (!entry.code.equalsIgnoreCase(code.trim())) {
            entry.attempts++;
            if (entry.attempts >= entry.maxAttempts) {
                challenges.remove(token.trim());
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不正确");
        }
        challenges.remove(token.trim());
    }

    private String randomCode(String alphabet, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CaptchaEntry>> iterator = challenges.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getValue().expireAt.isAfter(now)) {
                iterator.remove();
            }
        }
    }

    private int positiveOrDefault(int value, int defaultValue) {
        return value <= 0 ? defaultValue : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String safePurpose(String purpose) {
        return isBlank(purpose) ? "default" : purpose.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class CaptchaEntry {
        private final String appId;
        private final String purpose;
        private final String code;
        private final Instant expireAt;
        private final int maxAttempts;
        private int attempts;

        private CaptchaEntry(String appId, String purpose, String code, Instant expireAt, int maxAttempts) {
            this.appId = appId;
            this.purpose = purpose;
            this.code = code;
            this.expireAt = expireAt;
            this.maxAttempts = maxAttempts;
        }
    }
}
