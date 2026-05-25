package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateAdminUserRequest {
    @NotBlank
    private String username;
    @NotBlank
    @Size(min = 8, max = 64)
    private String password;
    private String displayName;

    public String username() { return username; }
    public String password() { return password; }
    public String displayName() { return displayName; }
}
