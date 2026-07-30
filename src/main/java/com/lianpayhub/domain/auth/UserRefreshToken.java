package com.lianpayhub.domain.auth;

import com.lianpayhub.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "user_refresh_token", indexes = {
        @Index(name = "idx_user_refresh_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_user_refresh_user_app", columnList = "user_id,app_id"),
        @Index(name = "idx_user_refresh_device", columnList = "app_id,user_id,device_code")
})
public class UserRefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "device_code", length = 128)
    private String deviceCode;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 128)
    private String revokeReason;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    protected UserRefreshToken() {
    }

    public UserRefreshToken(String tokenHash, Long userId, String appId, String deviceCode,
                            Long tokenVersion, LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.appId = appId;
        this.deviceCode = deviceCode;
        this.tokenVersion = tokenVersion;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public String getDeviceCode() { return deviceCode; }
    public Long getTokenVersion() { return tokenVersion; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public String getRevokeReason() { return revokeReason; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void markUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void revoke(String reason) {
        if (this.revokedAt == null) {
            this.revokedAt = LocalDateTime.now();
            this.revokeReason = reason;
        }
    }
}
