package com.lianpayhub.service.auth;

public class AppPasswordLoginCommand {
    private final String appId;
    private final String account;
    private final String password;
    private final String deviceCode;
    private final String ipAddress;
    private final String userAgent;

    public AppPasswordLoginCommand(String appId, String account, String password, String deviceCode, String ipAddress, String userAgent) {
        this.appId = appId;
        this.account = account;
        this.password = password;
        this.deviceCode = deviceCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public String appId() { return appId; }
    public String account() { return account; }
    public String password() { return password; }
    public String deviceCode() { return deviceCode; }
    public String ipAddress() { return ipAddress; }
    public String userAgent() { return userAgent; }
}
