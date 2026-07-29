package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class SetPasswordRequest {
    @NotBlank
    private String password;

    public String password() { return password; }
}
