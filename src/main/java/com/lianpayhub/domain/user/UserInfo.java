package com.lianpayhub.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lianpayhub.domain.BaseEntity;
import java.time.LocalDateTime;
import java.time.Duration;
import javax.persistence.*;

@Entity
@Table(name = "user_info", indexes = {
        @Index(name = "idx_user_info_mobile", columnList = "mobile", unique = true),
        @Index(name = "idx_user_info_username", columnList = "username", unique = true)
})
public class UserInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String mobile;

    @Column(length = 64, unique = true)
    private String username;

    @JsonIgnore
    @Column(name = "password_hash", length = 128)
    private String passwordHash;

    @Column(length = 128)
    private String nickname;

    @Column(name = "avatar_storage_key", length = 512)
    private String avatarStorageKey;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(name = "avatar_content_type", length = 128)
    private String avatarContentType;

    @Column(name = "avatar_size_bytes")
    private Long avatarSizeBytes;

    @Column(name = "password_set_at")
    private LocalDateTime passwordSetAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_password_attempts", nullable = false)
    private Integer failedPasswordAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 1L;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 32)
    private UserType userType = UserType.ACCOUNT;

    @Column(name = "open_id", length = 128)
    private String openId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status = UserStatus.ENABLED;

    protected UserInfo() {
    }

    public UserInfo(String mobile) {
        this.mobile = mobile;
    }

    public Long getId() {
        return id;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    @JsonIgnore
    public String getAvatarStorageKey() {
        return avatarStorageKey;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public Long getAvatarSizeBytes() {
        return avatarSizeBytes;
    }

    public LocalDateTime getPasswordSetAt() {
        return passwordSetAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    @JsonIgnore
    public Integer getFailedPasswordAttempts() {
        return failedPasswordAttempts;
    }

    @JsonIgnore
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    @JsonIgnore
    public Long getTokenVersion() {
        return tokenVersion;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getOpenId() {
        return openId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEnabled() {
        return status == UserStatus.ENABLED;
    }

    public boolean isLoginBlocked() {
        return status == UserStatus.DISABLED
                || status == UserStatus.LOCKED
                || status == UserStatus.DELETED;
    }

    public boolean isTemporarilyLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
        if (status == UserStatus.ENABLED) {
            this.lockedUntil = null;
            this.failedPasswordAttempts = 0;
        }
        this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
    }

    public void updateProfile(String mobile, String username, String nickname) {
        this.mobile = mobile;
        this.username = username;
        this.nickname = nickname;
    }

    public void updateProfile(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.passwordSetAt = LocalDateTime.now();
        this.mustChangePassword = false;
        this.failedPasswordAttempts = 0;
        this.lockedUntil = null;
        this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
    }

    public void updateAvatar(String avatarStorageKey, String avatarUrl, String avatarContentType, Long avatarSizeBytes) {
        this.avatarStorageKey = avatarStorageKey;
        this.avatarUrl = avatarUrl;
        this.avatarContentType = avatarContentType;
        this.avatarSizeBytes = avatarSizeBytes;
    }

    public void markLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedPasswordAttempts = 0;
        this.lockedUntil = null;
    }

    public void recordFailedPasswordAttempt(int maxAttempts, Duration lockDuration) {
        this.failedPasswordAttempts = this.failedPasswordAttempts == null ? 1 : this.failedPasswordAttempts + 1;
        if (this.failedPasswordAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plus(lockDuration);
            this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
        }
    }

    public void resetPasswordFailures() {
        this.failedPasswordAttempts = 0;
        this.lockedUntil = null;
    }

    public void requirePasswordReset() {
        this.mustChangePassword = true;
        this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
    }

    public void bumpTokenVersion() {
        this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
    }
}
