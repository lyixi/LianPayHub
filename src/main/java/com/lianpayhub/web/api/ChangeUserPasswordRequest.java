package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class ChangeUserPasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;

    public String oldPassword() { return oldPassword; }
    public String newPassword() { return newPassword; }
}
