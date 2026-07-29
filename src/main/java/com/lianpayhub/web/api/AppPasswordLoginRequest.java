package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class AppPasswordLoginRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String account;
    @NotBlank
    private String password;

    public String appId() { return appId; }
    public String account() { return account; }
    public String password() { return password; }
}
