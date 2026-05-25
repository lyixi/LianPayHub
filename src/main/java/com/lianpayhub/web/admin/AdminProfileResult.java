package com.lianpayhub.web.admin;

public class AdminProfileResult {

    private final Long adminId;
    private final String username;

    public AdminProfileResult(Long adminId, String username) {
        this.adminId = adminId;
        this.username = username;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }
}
