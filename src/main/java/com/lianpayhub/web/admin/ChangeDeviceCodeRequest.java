package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChangeDeviceCodeRequest {
    @NotBlank
    @Size(max = 128)
    private String deviceCode;

    @Size(max = 512)
    private String reason;

    public String deviceCode() {
        return deviceCode;
    }

    public String reason() {
        return reason;
    }
}
