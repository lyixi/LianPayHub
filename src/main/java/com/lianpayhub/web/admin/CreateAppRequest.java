package com.lianpayhub.web.admin;

import com.lianpayhub.domain.app.AppType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateAppRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String appName;
    @NotNull
    private AppType appType;
    private boolean needMobileLogin;
    private boolean needDeviceVip;
    private boolean allowPasswordLogin;
    private boolean allowAvatarUpload = true;
    private Integer accessTokenMinutes;
    private Integer refreshTokenMinutes;
    private boolean enableUserAiKey;
    private Long defaultAiQuotaUnits;
    private String defaultAiProviderCode;

    public String appId() { return appId; }
    public String appName() { return appName; }
    public AppType appType() { return appType; }
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
