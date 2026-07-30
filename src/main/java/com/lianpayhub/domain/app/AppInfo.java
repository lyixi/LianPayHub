package com.lianpayhub.domain.app;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "app_info", indexes = {
        @Index(name = "idx_app_info_app_id", columnList = "app_id", unique = true)
})
public class AppInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64, unique = true)
    private String appId;

    @Column(name = "app_name", nullable = false, length = 128)
    private String appName;

    @Column(name = "app_secret_hash", nullable = false, length = 128)
    private String appSecretHash;

    @Column(name = "app_secret_version", nullable = false)
    private Integer appSecretVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_type", nullable = false, length = 32)
    private AppType appType;

    @Column(name = "need_mobile_login", nullable = false)
    private boolean needMobileLogin;

    @Column(name = "need_device_vip", nullable = false)
    private boolean needDeviceVip;

    @Column(name = "allow_password_login", nullable = false)
    private boolean allowPasswordLogin;

    @Column(name = "allow_avatar_upload", nullable = false)
    private boolean allowAvatarUpload = true;

    @Column(name = "access_token_minutes", nullable = false)
    private Integer accessTokenMinutes = 30;

    @Column(name = "refresh_token_minutes", nullable = false)
    private Integer refreshTokenMinutes = 43200;

    @Column(name = "enable_user_ai_key", nullable = false)
    private boolean enableUserAiKey;

    @Column(name = "default_ai_quota_units", nullable = false)
    private Long defaultAiQuotaUnits = 0L;

    @Column(name = "default_ai_provider_code", length = 64)
    private String defaultAiProviderCode;

    @Column(name = "default_ai_group_id", length = 128)
    private String defaultAiGroupId;

    @Column(name = "default_ai_daily_limit", nullable = false)
    private Long defaultAiDailyLimit = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppStatus status = AppStatus.ENABLED;

    protected AppInfo() {
    }

    public AppInfo(String appId, String appName, String appSecretHash, AppType appType,
                   boolean needMobileLogin, boolean needDeviceVip) {
        this(appId, appName, appSecretHash, appType, needMobileLogin, needDeviceVip, false, 0L, null, null, 0L);
    }

    public AppInfo(String appId, String appName, String appSecretHash, AppType appType,
                   boolean needMobileLogin, boolean needDeviceVip,
                   boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode,
                   String defaultAiGroupId, Long defaultAiDailyLimit) {
        this(appId, appName, appSecretHash, appType, needMobileLogin, needDeviceVip, false, true,
                30, 43200, enableUserAiKey, defaultAiQuotaUnits, defaultAiProviderCode, defaultAiGroupId, defaultAiDailyLimit);
    }

    public AppInfo(String appId, String appName, String appSecretHash, AppType appType,
                   boolean needMobileLogin, boolean needDeviceVip,
                   boolean allowPasswordLogin, boolean allowAvatarUpload,
                   Integer accessTokenMinutes, Integer refreshTokenMinutes,
                   boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode,
                   String defaultAiGroupId, Long defaultAiDailyLimit) {
        this.appId = appId;
        this.appName = appName;
        this.appSecretHash = appSecretHash;
        this.appType = appType;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
        this.allowPasswordLogin = allowPasswordLogin;
        this.allowAvatarUpload = allowAvatarUpload;
        this.accessTokenMinutes = normalizeTokenMinutes(accessTokenMinutes, 30);
        this.refreshTokenMinutes = normalizeTokenMinutes(refreshTokenMinutes, 43200);
        this.enableUserAiKey = enableUserAiKey;
        this.defaultAiQuotaUnits = defaultAiQuotaUnits == null ? 0L : defaultAiQuotaUnits;
        this.defaultAiProviderCode = defaultAiProviderCode;
        this.defaultAiGroupId = defaultAiGroupId;
        this.defaultAiDailyLimit = defaultAiDailyLimit == null ? 0L : defaultAiDailyLimit;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public AppType getAppType() {
        return appType;
    }

    public boolean isNeedMobileLogin() {
        return needMobileLogin;
    }

    public boolean isNeedDeviceVip() {
        return needDeviceVip;
    }

    public boolean isAllowPasswordLogin() {
        return allowPasswordLogin;
    }

    public boolean isAllowAvatarUpload() {
        return allowAvatarUpload;
    }

    public Integer getAccessTokenMinutes() { return accessTokenMinutes; }

    public Integer getRefreshTokenMinutes() { return refreshTokenMinutes; }

    public boolean isEnableUserAiKey() { return enableUserAiKey; }
    public Long getDefaultAiQuotaUnits() { return defaultAiQuotaUnits; }
    public String getDefaultAiProviderCode() { return defaultAiProviderCode; }
    public String getDefaultAiGroupId() { return defaultAiGroupId; }
    public Long getDefaultAiDailyLimit() { return defaultAiDailyLimit; }

    public AppStatus getStatus() {
        return status;
    }

    public Integer getAppSecretVersion() {
        return appSecretVersion;
    }

    public String getAppSecretHash() {
        return appSecretHash;
    }

    public void rename(String appName) {
        this.appName = appName;
    }

    public void update(String appName, boolean needMobileLogin, boolean needDeviceVip) {
        update(appName, needMobileLogin, needDeviceVip, allowPasswordLogin, allowAvatarUpload);
    }

    public void update(String appName, boolean needMobileLogin, boolean needDeviceVip,
                       boolean allowPasswordLogin, boolean allowAvatarUpload) {
        update(appName, needMobileLogin, needDeviceVip, allowPasswordLogin, allowAvatarUpload,
                accessTokenMinutes, refreshTokenMinutes);
    }

    public void update(String appName, boolean needMobileLogin, boolean needDeviceVip,
                       boolean allowPasswordLogin, boolean allowAvatarUpload,
                       Integer accessTokenMinutes, Integer refreshTokenMinutes) {
        this.appName = appName;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
        this.allowPasswordLogin = allowPasswordLogin;
        this.allowAvatarUpload = allowAvatarUpload;
        this.accessTokenMinutes = normalizeTokenMinutes(accessTokenMinutes, 30);
        this.refreshTokenMinutes = normalizeTokenMinutes(refreshTokenMinutes, 43200);
    }

    public void updateAiSettings(boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode,
                                 String defaultAiGroupId, Long defaultAiDailyLimit) {
        this.enableUserAiKey = enableUserAiKey;
        this.defaultAiQuotaUnits = defaultAiQuotaUnits == null ? 0L : defaultAiQuotaUnits;
        this.defaultAiProviderCode = defaultAiProviderCode;
        this.defaultAiGroupId = defaultAiGroupId;
        this.defaultAiDailyLimit = defaultAiDailyLimit == null ? 0L : defaultAiDailyLimit;
    }

    public void changeStatus(AppStatus status) {
        this.status = status;
    }

    public void resetSecret(String appSecretHash) {
        this.appSecretHash = appSecretHash;
        this.appSecretVersion = this.appSecretVersion + 1;
    }

    private Integer normalizeTokenMinutes(Integer minutes, int fallback) {
        if (minutes == null || minutes < 1) {
            return fallback;
        }
        return Math.min(minutes, 525600);
    }
}
