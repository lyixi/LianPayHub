package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class RegisterDeviceRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String deviceFingerprint;

    public String appId() { return appId; }
    public String deviceCode() { return deviceCode; }
    public String deviceName() { return deviceName; }
    public String deviceType() { return deviceType; }
    public String deviceFingerprint() { return deviceFingerprint; }
}
