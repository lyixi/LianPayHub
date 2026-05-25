package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class SendCodeRequest {
    @NotBlank
    private String appId;
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;

    public String appId() { return appId; }
    public String mobile() { return mobile; }
}
