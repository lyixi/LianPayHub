package com.lianpayhub.web.admin;

import com.lianpayhub.domain.user.BindingStatus;
import javax.validation.constraints.NotNull;

public class ChangeUserAppBindingStatusRequest {
    @NotNull
    private BindingStatus status;

    public BindingStatus status() {
        return status;
    }
}
