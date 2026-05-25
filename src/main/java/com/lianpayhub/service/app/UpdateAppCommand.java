package com.lianpayhub.service.app;

public class UpdateAppCommand {
    private final String appName;
    private final boolean needMobileLogin;
    private final boolean needDeviceVip;

    public UpdateAppCommand(String appName, boolean needMobileLogin, boolean needDeviceVip) {
        this.appName = appName;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
    }

    public String appName() { return appName; }
    public boolean needMobileLogin() { return needMobileLogin; }
    public boolean needDeviceVip() { return needDeviceVip; }
}
