package com.lianpayhub.service.app;

public class ResetAppSecretResult {
    private final Long id;
    private final String appId;
    private final Integer appSecretVersion;
    private final String appSecret;

    public ResetAppSecretResult(Long id, String appId, Integer appSecretVersion, String appSecret) {
        this.id = id;
        this.appId = appId;
        this.appSecretVersion = appSecretVersion;
        this.appSecret = appSecret;
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public Integer getAppSecretVersion() { return appSecretVersion; }
    public String getAppSecret() { return appSecret; }
}
