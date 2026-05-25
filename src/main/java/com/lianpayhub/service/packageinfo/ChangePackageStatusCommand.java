package com.lianpayhub.service.packageinfo;

import com.lianpayhub.domain.packageinfo.PackageStatus;

public class ChangePackageStatusCommand {
    private final PackageStatus status;

    public ChangePackageStatusCommand(PackageStatus status) {
        this.status = status;
    }

    public PackageStatus status() { return status; }
}
