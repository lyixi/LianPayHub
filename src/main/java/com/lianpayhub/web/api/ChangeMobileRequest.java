package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class ChangeMobileRequest {
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String newMobile;

    @NotBlank
    private String oldCode;

    @NotBlank
    private String newCode;

    public String newMobile() { return newMobile; }
    public String oldCode() { return oldCode; }
    public String newCode() { return newCode; }
}
