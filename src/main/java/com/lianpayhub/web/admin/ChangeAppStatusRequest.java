package com.lianpayhub.web.admin;

import com.lianpayhub.domain.app.AppStatus;
import javax.validation.constraints.NotNull;

public class ChangeAppStatusRequest {
    @NotNull
    private AppStatus status;

    public AppStatus status() { return status; }
}
