package com.lianpayhub.domain.device;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_info", indexes = {
        @Index(name = "idx_device_info_app_device", columnList = "app_id,device_code", unique = true)
})
public class DeviceInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_code", nullable = false, length = 128)
    private String deviceCode;

    @Column(name = "device_name", length = 128)
    private String deviceName;

    @Column(name = "device_type", length = 64)
    private String deviceType;

    @Column(name = "device_fingerprint", length = 256)
    private String deviceFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "bind_status", nullable = false, length = 32)
    private DeviceBindStatus bindStatus = DeviceBindStatus.UNBOUND;

    @Column(name = "bind_at")
    private LocalDateTime bindAt;

    @Column(name = "last_launch_at")
    private LocalDateTime lastLaunchAt;

    protected DeviceInfo() {
    }

    public DeviceInfo(String appId, String deviceCode, String deviceName, String deviceType, String deviceFingerprint) {
        this.appId = appId;
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceFingerprint = deviceFingerprint;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public DeviceBindStatus getBindStatus() {
        return bindStatus;
    }

    public LocalDateTime getBindAt() {
        return bindAt;
    }

    public LocalDateTime getLastLaunchAt() {
        return lastLaunchAt;
    }

    public void bindUser(Long userId) {
        this.userId = userId;
        this.bindStatus = DeviceBindStatus.BOUND;
        this.bindAt = LocalDateTime.now();
    }

    public void unbindUser() {
        this.userId = null;
        this.bindStatus = DeviceBindStatus.UNBOUND;
        this.bindAt = null;
    }

    public void markLaunch() {
        this.lastLaunchAt = LocalDateTime.now();
    }
}
