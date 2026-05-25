package com.lianpayhub.domain.launch;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "launch_record", indexes = {
        @Index(name = "idx_launch_record_app_time", columnList = "app_id,created_at"),
        @Index(name = "idx_launch_record_device", columnList = "device_id")
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
    private String version;

    @Column(name = "network_type", length = 64)
    private String networkType;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private LaunchEventType eventType;

    @Lob
    @Column(name = "event_data")
    private String eventData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LaunchRecord() {
    }

    public LaunchRecord(String appId, Long deviceId, Long userId, String platform, String version,
                        String networkType, String ipAddress, LaunchEventType eventType, String eventData) {
        this.appId = appId;
        this.deviceId = deviceId;
        this.userId = userId;
        this.platform = platform;
        this.version = version;
        this.networkType = networkType;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
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

    public String getEventData() {
        return eventData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
