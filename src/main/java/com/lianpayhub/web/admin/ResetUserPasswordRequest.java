package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class ResetUserPasswordRequest {
    @NotBlank
    private String password;

    public String password() { return password; }
}
