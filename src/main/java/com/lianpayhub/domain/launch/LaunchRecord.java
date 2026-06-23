package com.lianpayhub.domain.launch;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "launch_record", indexes = {
        @Index(name = "idx_launch_record_app_time", columnList = "app_id,created_at"),
        @Index(name = "idx_launch_record_device", columnList = "device_id"),
        @Index(name = "idx_launch_record_session", columnList = "session_id")
})
public class LaunchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 64)
    private String platform;

    @Column(length = 64)
    private String branch;

    @Column(length = 64)
    private String channel;

    @Column(name = "platform_environment", length = 64)
    private String platformEnvironment;

    @Column(name = "version_name", length = 128)
    private String versionName;

    @Column(name = "version_code", length = 64)
    private String versionCode;

    @Column(length = 64)
    private String version;

    @Column(name = "network_type", length = 64)
    private String networkType;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private LaunchEventType eventType;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "session_start_at")
    private LocalDateTime sessionStartAt;

    @Column(name = "session_end_at")
    private LocalDateTime sessionEndAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Lob
    @Column(name = "event_data")
    private String eventData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LaunchRecord() {
    }

    public LaunchRecord(String appId, Long deviceId, Long userId, String platform, String version,
                        String networkType, String ipAddress, LaunchEventType eventType, String eventData) {
        this(appId, deviceId, userId, platform, null, null, null, null, null, version, networkType, ipAddress, eventType, null, null, null, null, eventData);
    }

    public LaunchRecord(String appId, Long deviceId, Long userId, String platform, String version,
                        String networkType, String ipAddress, LaunchEventType eventType,
                        LocalDateTime sessionStartAt, LocalDateTime sessionEndAt,
                        Long durationSeconds, String eventData) {
        this(appId, deviceId, userId, platform, null, null, null, null, null, version, networkType, ipAddress, eventType, null,
                sessionStartAt, sessionEndAt, durationSeconds, eventData);
    }

    public LaunchRecord(String appId, Long deviceId, Long userId, String platform, String branch, String channel,
                        String platformEnvironment, String versionName, String versionCode, String version,
                        String networkType, String ipAddress, LaunchEventType eventType, String sessionId,
                        LocalDateTime sessionStartAt, LocalDateTime sessionEndAt,
                        Long durationSeconds, String eventData) {
        this.appId = appId;
        this.deviceId = deviceId;
        this.userId = userId;
        this.platform = platform;
        this.branch = branch;
        this.channel = channel;
        this.platformEnvironment = platformEnvironment;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.version = version;
        this.networkType = networkType;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.sessionStartAt = sessionStartAt;
        this.sessionEndAt = sessionEndAt;
        this.durationSeconds = durationSeconds;
        this.eventData = eventData;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getBranch() { return branch; }
    public String getChannel() { return channel; }
    public String getPlatformEnvironment() { return platformEnvironment; }
    public String getVersionName() { return versionName; }
    public String getVersionCode() { return versionCode; }

    public String getVersion() {
        return version;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LaunchEventType getEventType() {
        return eventType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getSessionStartAt() {
        return sessionStartAt;
    }

    public LocalDateTime getSessionEndAt() {
        return sessionEndAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public String getEventData() {
        return eventData;
    }

    public void completeSession(LocalDateTime sessionEndAt, Long durationSeconds) {
        this.sessionStartAt = this.sessionStartAt == null ? this.createdAt : this.sessionStartAt;
        this.sessionEndAt = sessionEndAt;
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
