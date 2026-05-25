package com.lianpayhub.web.admin;

import com.lianpayhub.domain.admin.AdminUserStatus;
import javax.validation.constraints.NotNull;

public class ChangeAdminUserStatusRequest {
    @NotNull
    private AdminUserStatus status;

    public AdminUserStatus status() { return status; }
}
