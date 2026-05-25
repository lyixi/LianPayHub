package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetAdminPasswordRequest {
    @NotBlank
    @Size(min = 8, max = 64)
    private String newPassword;

    public String newPassword() { return newPassword; }
}
