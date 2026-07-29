package com.lianpayhub.service.app;

import com.lianpayhub.domain.app.AppType;

public class CreateAppCommand {
    private final String appId;
    private final String appName;
    private final AppType appType;
    private final boolean needMobileLogin;
    private final boolean needDeviceVip;
    private final boolean allowPasswordLogin;
    private final boolean allowAvatarUpload;
    private final boolean enableUserAiKey;
    private final Long defaultAiQuotaUnits;
    private final String defaultAiProviderCode;

    public CreateAppCommand(String appId, String appName, AppType appType,
                            boolean needMobileLogin, boolean needDeviceVip,
                            boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode) {
        this(appId, appName, appType, needMobileLogin, needDeviceVip, false, true,
                enableUserAiKey, defaultAiQuotaUnits, defaultAiProviderCode);
    }

    public CreateAppCommand(String appId, String appName, AppType appType,
                            boolean needMobileLogin, boolean needDeviceVip,
                            boolean allowPasswordLogin, boolean allowAvatarUpload,
                            boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode) {
        this.appId = appId;
        this.appName = appName;
        this.appType = appType;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
        this.allowPasswordLogin = allowPasswordLogin;
        this.allowAvatarUpload = allowAvatarUpload;
        this.enableUserAiKey = enableUserAiKey;
        this.defaultAiQuotaUnits = defaultAiQuotaUnits;
        this.defaultAiProviderCode = defaultAiProviderCode;
    }

    public String appId() { return appId; }
    public String appName() { return appName; }
    public AppType appType() { return appType; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
    public boolean allowPasswordLogin() { return allowPasswordLogin; }
    public boolean allowAvatarUpload() { return allowAvatarUpload; }
    public boolean enableUserAiKey() { return enableUserAiKey; }
    public Long defaultAiQuotaUnits() { return defaultAiQuotaUnits; }
    public String defaultAiProviderCode() { return defaultAiProviderCode; }
}
