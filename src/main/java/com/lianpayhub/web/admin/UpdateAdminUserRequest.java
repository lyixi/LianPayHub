package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class UpdateAdminUserRequest {
    @NotBlank
    private String displayName;

    public String displayName() { return displayName; }
}
