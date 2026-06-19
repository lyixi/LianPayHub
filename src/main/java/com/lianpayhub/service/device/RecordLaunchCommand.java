package com.lianpayhub.service.device;

import java.time.LocalDateTime;

public class RecordLaunchCommand {
    private final String appId;
    private final String deviceCode;
    private final Long userId;
    private final String platform;
    private final String version;
    private final String networkType;
    private final String ipAddress;
    private final String sessionId;
    private final String previousSessionId;
    private final LocalDateTime previousSessionStartAt;
    private final LocalDateTime previousSessionEndAt;
    private final Long previousDurationSeconds;
    private final String eventData;

    public RecordLaunchCommand(String appId, String deviceCode, Long userId, String platform,
                               String version, String networkType, String ipAddress,
                               String sessionId, String previousSessionId,
                               LocalDateTime previousSessionStartAt, LocalDateTime previousSessionEndAt,
                               Long previousDurationSeconds, String eventData) {
        this.appId = appId;
        this.deviceCode = deviceCode;
        this.userId = userId;
        this.platform = platform;
        this.version = version;
        this.networkType = networkType;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        this.previousSessionId = previousSessionId;
        this.previousSessionStartAt = previousSessionStartAt;
        this.previousSessionEndAt = previousSessionEndAt;
        this.previousDurationSeconds = previousDurationSeconds;
        this.eventData = eventData;
    }

    public String appId() { return appId; }
    public String deviceCode() { return deviceCode; }
    public Long userId() { return userId; }
    public String platform() { return platform; }
    public String version() { return version; }
    public String networkType() { return networkType; }
    public String ipAddress() { return ipAddress; }
    public String sessionId() { return sessionId; }
    public String previousSessionId() { return previousSessionId; }
    public LocalDateTime previousSessionStartAt() { return previousSessionStartAt; }
    public LocalDateTime previousSessionEndAt() { return previousSessionEndAt; }
    public Long previousDurationSeconds() { return previousDurationSeconds; }
    public String eventData() { return eventData; }
}
