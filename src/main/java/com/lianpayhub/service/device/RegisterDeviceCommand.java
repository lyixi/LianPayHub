package com.lianpayhub.service.device;

public class RegisterDeviceCommand {
    private final String appId;
    private final String deviceCode;
    private final String deviceName;
    private final String deviceType;
    private final String deviceFingerprint;

    public RegisterDeviceCommand(String appId, String deviceCode, String deviceName,
                                 String deviceType, String deviceFingerprint) {
        this.appId = appId;
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceFingerprint = deviceFingerprint;
    }

    public String appId() { return appId; }
    public String deviceCode() { return deviceCode; }
    public String deviceName() { return deviceName; }
    public String deviceType() { return deviceType; }
    public String deviceFingerprint() { return deviceFingerprint; }
}
