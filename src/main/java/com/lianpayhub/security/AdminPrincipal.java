package com.lianpayhub.security;

public class AdminPrincipal {

    private final Long adminId;
    private final String username;

    public AdminPrincipal(Long adminId, String username) {
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
