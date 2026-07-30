package com.lianpayhub.service.auth;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.auth.UserRefreshToken;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppStatus;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.device.DeviceBindStatus;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.repository.UserRefreshTokenRepository;
import com.lianpayhub.service.security.AppSecretService;
import com.lianpayhub.service.security.JwtService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class UserRefreshTokenService {

    private final UserRefreshTokenRepository refreshTokenRepository;
    private final UserInfoRepository userInfoRepository;
    private final AppInfoRepository appInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final AppSecretService appSecretService;
    private final JwtService jwtService;

    public UserRefreshTokenService(UserRefreshTokenRepository refreshTokenRepository,
                                   UserInfoRepository userInfoRepository,
                                   AppInfoRepository appInfoRepository,
                                   DeviceInfoRepository deviceInfoRepository,
                                   AppSecretService appSecretService,
                                   JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userInfoRepository = userInfoRepository;
        this.appInfoRepository = appInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.appSecretService = appSecretService;
        this.jwtService = jwtService;
    }

    @Transactional
    public IssuedTokens issue(UserInfo userInfo, AppInfo appInfo, String deviceCode, String ipAddress, String userAgent) {
        String safeDeviceCode = trimToNull(deviceCode);
        if (safeDeviceCode != null) {
            DeviceInfo device = deviceInfoRepository.findByAppIdAndDeviceCode(appInfo.getAppId(), safeDeviceCode)
                    .orElse(null);
            if (device != null && device.getBindStatus() == com.lianpayhub.domain.device.DeviceBindStatus.BLACKLISTED) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "设备已被拉黑");
            }
        }
        String refreshToken = generateRefreshToken();
        String tokenHash = appSecretService.hashSecret(refreshToken);
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusMinutes(safeMinutes(appInfo.getRefreshTokenMinutes(), 43200));
        UserRefreshToken record = new UserRefreshToken(tokenHash, userInfo.getId(), appInfo.getAppId(), safeDeviceCode,
                userInfo.getTokenVersion(), refreshExpireAt, ipAddress, userAgent);
        refreshTokenRepository.save(record);
        String accessToken = jwtService.generateUserToken(
                userInfo.getId(),
                appInfo.getAppId(),
                userInfo.getMobile(),
                safeDeviceCode,
                userInfo.getTokenVersion(),
                safeMinutes(appInfo.getAccessTokenMinutes(), 720)
        );
        return new IssuedTokens(accessToken, refreshToken, userInfo.getId(), appInfo.getAppId(), userInfo.getMobile(),
                userInfo.getTokenVersion(),
                safeMinutes(appInfo.getAccessTokenMinutes(), 720), safeMinutes(appInfo.getRefreshTokenMinutes(), 43200));
    }

    @Transactional
    public IssuedTokens refresh(String refreshToken) {
        String tokenHash = appSecretService.hashSecret(normalize(refreshToken));
        UserRefreshToken record = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "refresh token 无效或已过期"));
        if (!record.isActive()) {
            if ("rotated".equalsIgnoreCase(record.getRevokeReason())) {
                revokeByUserAndApp(record.getUserId(), record.getAppId(), "refresh_token_reuse_detected");
                revokeByUserAndDevice(record.getUserId(), record.getAppId(), record.getDeviceCode(), "refresh_token_reuse_detected");
                UserInfo userInfo = userInfoRepository.findById(record.getUserId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "用户不存在"));
                userInfo.bumpTokenVersion();
                userInfoRepository.save(userInfo);
            }
            throw new BusinessException(ErrorCode.FORBIDDEN, "refresh token 无效或已过期");
        }
        UserInfo userInfo = userInfoRepository.findById(record.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "用户不存在"));
        if (!userInfo.isEnabled() || userInfo.getTokenVersion() == null || !userInfo.getTokenVersion().equals(record.getTokenVersion())) {
            revoke(record, "token_version_mismatch");
            throw new BusinessException(ErrorCode.FORBIDDEN, "refresh token 已失效");
        }
        AppInfo appInfo = appInfoRepository.findByAppId(record.getAppId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "APP 不存在"));
        if (appInfo.getStatus() != AppStatus.ENABLED) {
            revoke(record, "app_disabled");
            throw new BusinessException(ErrorCode.FORBIDDEN, "APP 已停用");
        }
        if (record.getDeviceCode() != null) {
            DeviceInfo device = deviceInfoRepository.findByAppIdAndDeviceCode(record.getAppId(), record.getDeviceCode())
                    .orElse(null);
            if (device != null && device.getBindStatus() == DeviceBindStatus.BLACKLISTED) {
                revoke(record, "device_blacklisted");
                throw new BusinessException(ErrorCode.FORBIDDEN, "设备已被拉黑");
            }
        }
        record.markUsed();
        record.revoke("rotated");
        refreshTokenRepository.save(record);
        return issue(userInfo, appInfo, record.getDeviceCode(), record.getIpAddress(), record.getUserAgent());
    }

    @Transactional
    public void revokeByToken(String refreshToken, String reason) {
        UserRefreshToken record = findActive(refreshToken);
        revoke(record, reason);
    }

    @Transactional
    public void revokeByUser(Long userId, String reason) {
        for (UserRefreshToken record : refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId)) {
            revoke(record, reason);
        }
    }

    @Transactional
    public void revokeByUserAndDevice(Long userId, String appId, String deviceCode, String reason) {
        for (UserRefreshToken record : refreshTokenRepository.findByUserIdAndAppIdAndDeviceCodeAndRevokedAtIsNull(
                userId, appId, trimToNull(deviceCode))) {
            revoke(record, reason);
        }
    }

    @Transactional
    public void revokeByAppDevice(String appId, String deviceCode, String reason) {
        for (UserRefreshToken record : refreshTokenRepository.findByAppIdAndDeviceCodeAndRevokedAtIsNull(appId, trimToNull(deviceCode))) {
            revoke(record, reason);
        }
    }

    @Transactional
    public void revokeByUserAndApp(Long userId, String appId, String reason) {
        for (UserRefreshToken record : refreshTokenRepository.findByUserIdAndAppIdAndRevokedAtIsNull(userId, appId)) {
            revoke(record, reason);
        }
    }

    private UserRefreshToken findActive(String refreshToken) {
        String tokenHash = appSecretService.hashSecret(normalize(refreshToken));
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(UserRefreshToken::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "refresh token 无效或已过期"));
    }

    private void revoke(UserRefreshToken record, String reason) {
        record.revoke(reason);
        refreshTokenRepository.save(record);
    }

    @Scheduled(cron = "0 15 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiresAtBeforeOrRevokedAtBefore(now.minusDays(90), now.minusDays(90));
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "refreshToken 不能为空");
        }
        return trimmed;
    }

    private Integer safeMinutes(Integer minutes, int fallback) {
        if (minutes == null || minutes < 1) {
            return fallback;
        }
        return minutes;
    }

    public static class IssuedTokens {
        private final String accessToken;
        private final String refreshToken;
        private final Long userId;
        private final String appId;
        private final String mobile;
        private final Long tokenVersion;
        private final Integer accessTokenExpiresInMinutes;
        private final Integer refreshTokenExpiresInMinutes;

        public IssuedTokens(String accessToken, String refreshToken, Long userId, String appId, String mobile, Long tokenVersion,
                            Integer accessTokenExpiresInMinutes, Integer refreshTokenExpiresInMinutes) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.appId = appId;
            this.mobile = mobile;
            this.tokenVersion = tokenVersion;
            this.accessTokenExpiresInMinutes = accessTokenExpiresInMinutes;
            this.refreshTokenExpiresInMinutes = refreshTokenExpiresInMinutes;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public Long getUserId() { return userId; }
        public String getAppId() { return appId; }
        public String getMobile() { return mobile; }
        public Long getTokenVersion() { return tokenVersion; }
        public Integer getAccessTokenExpiresInMinutes() { return accessTokenExpiresInMinutes; }
        public Integer getRefreshTokenExpiresInMinutes() { return refreshTokenExpiresInMinutes; }
    }
}
