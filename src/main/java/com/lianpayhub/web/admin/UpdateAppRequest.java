package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class UpdateAppRequest {
    @NotBlank
    private String appName;
    private boolean needMobileLogin;
    private boolean needDeviceVip;

    public String appName() { return appName; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
}
