package com.lianpayhub.web.admin;

import javax.validation.constraints.NotNull;

public class BindDeviceUserRequest {
    @NotNull
    private Long userId;

    public Long userId() {
        return userId;
    }
}
