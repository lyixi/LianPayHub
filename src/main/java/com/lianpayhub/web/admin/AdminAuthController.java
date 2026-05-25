package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AdminPrincipal;
import com.lianpayhub.service.admin.AdminAuthService;
import com.lianpayhub.service.admin.AdminLoginResult;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<AdminProfileResult> me(@AuthenticationPrincipal AdminPrincipal principal) {
        return ApiResponse.ok(new AdminProfileResult(principal.getAdminId(), principal.getUsername()));
    }
}
