package com.lianpayhub.security;

public class AppUserPrincipal {
    private final Long userId;
    private final String appId;
    private final String mobile;

    public AppUserPrincipal(Long userId, String appId, String mobile) {
        this.userId = userId;
        this.appId = appId;
        this.mobile = mobile;
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
}
