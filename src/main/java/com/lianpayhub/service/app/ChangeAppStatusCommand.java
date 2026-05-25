package com.lianpayhub.service.app;

import com.lianpayhub.domain.app.AppStatus;

public class ChangeAppStatusCommand {
    private final AppStatus status;

    public ChangeAppStatusCommand(AppStatus status) {
        this.status = status;
    }

    public AppStatus status() { return status; }
}
