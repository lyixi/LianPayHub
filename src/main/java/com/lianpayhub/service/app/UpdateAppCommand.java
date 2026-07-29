package com.lianpayhub.service.app;

public class UpdateAppCommand {
    private final String appName;
    private final boolean needMobileLogin;
    private final boolean needDeviceVip;
    private final boolean enableUserAiKey;
    private final Long defaultAiQuotaUnits;
    private final String defaultAiProviderCode;

    public UpdateAppCommand(String appName, boolean needMobileLogin, boolean needDeviceVip,
                            boolean enableUserAiKey, Long defaultAiQuotaUnits, String defaultAiProviderCode) {
        this.appName = appName;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
        this.enableUserAiKey = enableUserAiKey;
        this.defaultAiQuotaUnits = defaultAiQuotaUnits;
        this.defaultAiProviderCode = defaultAiProviderCode;
    }

    public String appName() { return appName; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
    public boolean enableUserAiKey() { return enableUserAiKey; }
    public Long defaultAiQuotaUnits() { return defaultAiQuotaUnits; }
    public String defaultAiProviderCode() { return defaultAiProviderCode; }
}
