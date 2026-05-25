package com.lianpayhub.web.admin;

import com.lianpayhub.domain.user.BindType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateUserAppBindingRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String appId;
    private BindType bindType;

    public Long userId() {
        return userId;
    }

    public String appId() {
        return appId;
    }

    public BindType bindType() {
        return bindType;
    }
}
