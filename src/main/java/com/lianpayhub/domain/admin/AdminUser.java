package com.lianpayhub.domain.admin;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_user", indexes = {
        @Index(name = "idx_admin_user_username", columnList = "username", unique = true)
})
public class AdminUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdminUserStatus status = AdminUserStatus.ENABLED;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    protected AdminUser() {
    }

    public AdminUser(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AdminUserStatus getStatus() {
        return status;
    }

    public boolean isEnabled() {
        return status == AdminUserStatus.ENABLED;
    }

    public void markLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void changeDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void changeStatus(AdminUserStatus status) {
        this.status = status;
    }

    public void resetPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
