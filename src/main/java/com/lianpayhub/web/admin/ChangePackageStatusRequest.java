package com.lianpayhub.web.admin;

import com.lianpayhub.domain.packageinfo.PackageStatus;
import javax.validation.constraints.NotNull;

public class ChangePackageStatusRequest {
    @NotNull
    private PackageStatus status;

    public PackageStatus status() { return status; }
}
