package com.lianpayhub.service.admin;

public class AdminLoginResult {

    private final String token;
    private final Long adminId;
    private final String username;
    private final String displayName;
    private final boolean mustChangePassword;

    public AdminLoginResult(String token, Long adminId, String username, String displayName) {
        this(token, adminId, username, displayName, false);
    }

    public AdminLoginResult(String token, Long adminId, String username, String displayName, boolean mustChangePassword) {
        this.token = token;
        this.adminId = adminId;
        this.username = username;
        this.displayName = displayName;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() {
        return token;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
}
