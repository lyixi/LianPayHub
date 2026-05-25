package com.lianpayhub.web.admin;

import com.lianpayhub.domain.user.UserStatus;

import javax.validation.constraints.NotNull;

public class ChangeUserStatusRequest {
    @NotNull
    private UserStatus status;

    public UserStatus status() { return status; }
}
