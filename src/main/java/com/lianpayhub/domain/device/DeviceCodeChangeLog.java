package com.lianpayhub.domain.device;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "device_code_change_log", indexes = {
        @Index(name = "idx_device_code_change_device", columnList = "device_id"),
        @Index(name = "idx_device_code_change_app", columnList = "app_id"),
        @Index(name = "idx_device_code_change_old", columnList = "old_device_code"),
        @Index(name = "idx_device_code_change_new", columnList = "new_device_code")
})
public class DeviceCodeChangeLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "old_device_code", nullable = false, length = 128)
    private String oldDeviceCode;

    @Column(name = "new_device_code", nullable = false, length = 128)
    private String newDeviceCode;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_username", length = 64)
    private String adminUsername;

    protected DeviceCodeChangeLog() {
    }

    public DeviceCodeChangeLog(Long deviceId, String appId, String oldDeviceCode, String newDeviceCode,
                               String reason, Long adminId, String adminUsername) {
        this.deviceId = deviceId;
        this.appId = appId;
        this.oldDeviceCode = oldDeviceCode;
        this.newDeviceCode = newDeviceCode;
        this.reason = reason;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
    }

    public Long getId() { return id; }
    public Long getDeviceId() { return deviceId; }
    public String getAppId() { return appId; }
    public String getOldDeviceCode() { return oldDeviceCode; }
    public String getNewDeviceCode() { return newDeviceCode; }
    public String getReason() { return reason; }
    public Long getAdminId() { return adminId; }
    public String getAdminUsername() { return adminUsername; }
}
