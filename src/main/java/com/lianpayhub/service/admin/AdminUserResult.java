package com.lianpayhub.service.admin;

import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.domain.admin.AdminUserStatus;
import java.time.LocalDateTime;

public class AdminUserResult {
    private final Long id;
    private final String username;
    private final String displayName;
    private final AdminUserStatus status;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminUserResult(AdminUser adminUser) {
        this.id = adminUser.getId();
        this.username = adminUser.getUsername();
        this.displayName = adminUser.getDisplayName();
        this.status = adminUser.getStatus();
        this.lastLoginAt = adminUser.getLastLoginAt();
        this.createdAt = adminUser.getCreatedAt();
        this.updatedAt = adminUser.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public AdminUserStatus getStatus() { return status; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
