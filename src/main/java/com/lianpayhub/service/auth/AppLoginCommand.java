package com.lianpayhub.service.auth;

public class AppLoginCommand {
    private final String appId;
    private final String mobile;
    private final String code;
    private final String ipAddress;
    private final String userAgent;

    public AppLoginCommand(String appId, String mobile, String code, String ipAddress, String userAgent) {
        this.appId = appId;
        this.mobile = mobile;
        this.code = code;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public String appId() { return appId; }
    public String mobile() { return mobile; }
    public String code() { return code; }
    public String ipAddress() { return ipAddress; }
    public String userAgent() { return userAgent; }
}
