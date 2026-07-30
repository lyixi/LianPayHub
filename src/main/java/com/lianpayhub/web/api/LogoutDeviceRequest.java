package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class LogoutDeviceRequest {
    @NotBlank
    private String deviceCode;

    public String deviceCode() { return deviceCode; }
}
