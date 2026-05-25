package com.lianpayhub.service.app;

public class CreateAppResult {
    private final Long id;
    private final String appId;
    private final String appSecret;

    public CreateAppResult(Long id, String appId, String appSecret) {
        this.id = id;
        this.appId = appId;
        this.appSecret = appSecret;
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public String getAppSecret() { return appSecret; }
}
