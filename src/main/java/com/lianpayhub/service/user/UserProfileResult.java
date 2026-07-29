package com.lianpayhub.service.user;

import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.domain.user.UserStatus;
import java.time.LocalDateTime;

public class UserProfileResult {
    private final Long id;
    private final String mobile;
    private final String username;
    private final String nickname;
    private final String avatarUrl;
    private final String avatarContentType;
    private final Long avatarSizeBytes;
    private final UserStatus status;
    private final LocalDateTime passwordSetAt;
    private final LocalDateTime lastLoginAt;
    private final boolean mustChangePassword;

    public UserProfileResult(UserInfo userInfo) {
        this.id = userInfo.getId();
        this.mobile = userInfo.getMobile();
        this.username = userInfo.getUsername();
        this.nickname = userInfo.getNickname();
        this.avatarUrl = userInfo.getAvatarUrl();
        this.avatarContentType = userInfo.getAvatarContentType();
        this.avatarSizeBytes = userInfo.getAvatarSizeBytes();
        this.status = userInfo.getStatus();
        this.passwordSetAt = userInfo.getPasswordSetAt();
        this.lastLoginAt = userInfo.getLastLoginAt();
        this.mustChangePassword = userInfo.isMustChangePassword();
    }

    public Long getId() { return id; }
    public String getMobile() { return mobile; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getAvatarContentType() { return avatarContentType; }
    public Long getAvatarSizeBytes() { return avatarSizeBytes; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getPasswordSetAt() { return passwordSetAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
