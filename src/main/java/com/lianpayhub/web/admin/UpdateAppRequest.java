package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class UpdateAppRequest {
    @NotBlank
    private String appName;
    private boolean needMobileLogin;
    private boolean needDeviceVip;
    private boolean allowPasswordLogin;
    private boolean allowAvatarUpload = true;
    private Integer accessTokenMinutes;
    private Integer refreshTokenMinutes;
    private boolean enableUserAiKey;
    private Long defaultAiQuotaUnits;
    private String defaultAiProviderCode;

    public String appName() { return appName; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
    public boolean allowPasswordLogin() { return allowPasswordLogin; }
    public boolean allowAvatarUpload() { return allowAvatarUpload; }
    public Integer accessTokenMinutes() { return accessTokenMinutes; }
    public Integer refreshTokenMinutes() { return refreshTokenMinutes; }
    public boolean enableUserAiKey() { return enableUserAiKey; }
    public Long defaultAiQuotaUnits() { return defaultAiQuotaUnits; }
    public String defaultAiProviderCode() { return defaultAiProviderCode; }
}
