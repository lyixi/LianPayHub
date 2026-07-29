package com.lianpayhub.service.auth;

public class AppLoginResult {
    private final String token;
    private final Long userId;
    private final String mobile;
    private final String appId;
    private final boolean mustChangePassword;

    public AppLoginResult(String token, Long userId, String mobile, String appId, boolean mustChangePassword) {
        this.token = token;
        this.userId = userId;
        this.mobile = mobile;
        this.appId = appId;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getMobile() { return mobile; }
    public String getAppId() { return appId; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
