package com.lianpayhub.domain.config;

import javax.persistence.*;

@Entity
@Table(name = "user_config_version", indexes = {
        @Index(name = "idx_user_config_version_owner", columnList = "user_id,app_id", unique = true)
})
public class UserConfigVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "current_version", nullable = false)
    private Long currentVersion = 0L;

    protected UserConfigVersion() {
    }

    public UserConfigVersion(Long userId, String appId) {
        this.userId = userId;
        this.appId = appId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public Long getCurrentVersion() { return currentVersion; }

    public Long nextVersion() {
        this.currentVersion = this.currentVersion == null ? 1L : this.currentVersion + 1L;
        return this.currentVersion;
    }
}
