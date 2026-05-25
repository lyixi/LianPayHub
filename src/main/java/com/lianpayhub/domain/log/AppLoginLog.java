package com.lianpayhub.domain.log;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_login_log", indexes = {
        @Index(name = "idx_app_login_app_time", columnList = "app_id,created_at"),
        @Index(name = "idx_app_login_user", columnList = "user_id")
})
public class AppLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 32)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 32)
    private AppLoginType loginType;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_code", length = 128)
    private String deviceCode;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 32)
    private LogResultStatus resultStatus;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AppLoginLog() {
    }

    public AppLoginLog(String appId, Long userId, String mobile, AppLoginType loginType, Long deviceId,
                       String deviceCode, String ipAddress, String userAgent, LogResultStatus resultStatus,
                       String errorMessage) {
        this.appId = appId;
        this.userId = userId;
        this.mobile = mobile;
        this.loginType = loginType;
        this.deviceId = deviceId;
        this.deviceCode = deviceCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.resultStatus = resultStatus;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public Long getUserId() { return userId; }
    public String getMobile() { return mobile; }
    public AppLoginType getLoginType() { return loginType; }
    public Long getDeviceId() { return deviceId; }
    public String getDeviceCode() { return deviceCode; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LogResultStatus getResultStatus() { return resultStatus; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
