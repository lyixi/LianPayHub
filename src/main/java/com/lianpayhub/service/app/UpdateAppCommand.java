package com.lianpayhub.service.app;

public class UpdateAppCommand {
    private final String appName;
    private final boolean needMobileLogin;
    private final boolean needDeviceVip;
    private final boolean allowPasswordLogin;
    private final boolean allowAvatarUpload;
    private final boolean enableUserAiKey;
    private final Long defaultAiQuotaUnits;
    private final String defaultAiProviderCode;

    public UpdateAppCommand(String appName, boolean needMobileLogin, boolean needDeviceVip,
                            boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode) {
        this(appName, needMobileLogin, needDeviceVip, false, true,
                enableUserAiKey, defaultAiQuotaUnits, defaultAiProviderCode);
    }

    public UpdateAppCommand(String appName, boolean needMobileLogin, boolean needDeviceVip,
                            boolean allowPasswordLogin, boolean allowAvatarUpload,
                            boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode) {
        this.appName = appName;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
        this.allowPasswordLogin = allowPasswordLogin;
        this.allowAvatarUpload = allowAvatarUpload;
        this.enableUserAiKey = enableUserAiKey;
        this.defaultAiQuotaUnits = defaultAiQuotaUnits;
        this.defaultAiProviderCode = defaultAiProviderCode;
    }

    public String appName() { return appName; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
    public boolean allowPasswordLogin() { return allowPasswordLogin; }
    public boolean allowAvatarUpload() { return allowAvatarUpload; }
    public boolean enableUserAiKey() { return enableUserAiKey; }
    public Long defaultAiQuotaUnits() { return defaultAiQuotaUnits; }
    public String defaultAiProviderCode() { return defaultAiProviderCode; }
}
