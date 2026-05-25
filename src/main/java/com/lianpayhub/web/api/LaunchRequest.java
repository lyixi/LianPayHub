package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class LaunchRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String deviceCode;
    private Long userId;
    private String platform;
    private String version;
    private String networkType;
    private String ipAddress;
    private String eventData;

    public String appId() { return appId; }
    public String deviceCode() { return deviceCode; }
    public Long userId() { return userId; }
    public String platform() { return platform; }
    public String version() { return version; }
    public String networkType() { return networkType; }
    public String ipAddress() { return ipAddress; }
    public String eventData() { return eventData; }
}
