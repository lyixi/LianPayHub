package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class AdminLoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
