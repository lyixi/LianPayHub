package com.lianpayhub.web.admin;

import javax.validation.constraints.Pattern;

public class UpdateUserProfileAdminRequest {
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;
    private String username;
    private String nickname;

    public String mobile() { return mobile; }
    public String username() { return username; }
    public String nickname() { return nickname; }
}
