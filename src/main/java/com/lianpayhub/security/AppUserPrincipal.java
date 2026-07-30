package com.lianpayhub.security;

public class AppUserPrincipal {
    private final Long userId;
    private final String appId;
    private final String mobile;
    private final String deviceCode;

    public AppUserPrincipal(Long userId, String appId, String mobile, String deviceCode) {
        this.userId = userId;
        this.appId = appId;
        this.mobile = mobile;
        this.deviceCode = deviceCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAppId() {
        return appId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getDeviceCode() {
        return deviceCode;
    }
}
