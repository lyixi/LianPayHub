package com.lianpayhub.service.auth;

public class AppLoginResult {
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;
    private final String mobile;
    private final String appId;
    private final boolean mustChangePassword;
    private final Integer accessTokenExpiresInMinutes;
    private final Integer refreshTokenExpiresInMinutes;

    public AppLoginResult(String accessToken, String refreshToken, Long userId, String mobile, String appId,
                          boolean mustChangePassword, Integer accessTokenExpiresInMinutes,
                          Integer refreshTokenExpiresInMinutes) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.mobile = mobile;
        this.appId = appId;
        this.mustChangePassword = mustChangePassword;
        this.accessTokenExpiresInMinutes = accessTokenExpiresInMinutes;
        this.refreshTokenExpiresInMinutes = refreshTokenExpiresInMinutes;
    }

    public String getToken() { return accessToken; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Long getUserId() { return userId; }
    public String getMobile() { return mobile; }
    public String getAppId() { return appId; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public Integer getAccessTokenExpiresInMinutes() { return accessTokenExpiresInMinutes; }
    public Integer getRefreshTokenExpiresInMinutes() { return refreshTokenExpiresInMinutes; }
}
