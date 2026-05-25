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

    public String appId() { return appId; }
    public String appName() { return appName; }
    public AppType appType() { return appType; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
}
