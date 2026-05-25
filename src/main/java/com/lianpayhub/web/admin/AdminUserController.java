package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AdminPrincipal;
import com.lianpayhub.service.admin.AdminUserResult;
import com.lianpayhub.service.admin.AdminUserService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/admin-users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<Page<AdminUserResult>> list(@RequestParam(required = false) String username,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminUserService.list(username, pageRequest(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResult> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.detail(id));
    }

    @PostMapping
    public ApiResponse<AdminUserResult> create(@Valid @RequestBody CreateAdminUserRequest request) {
        return ApiResponse.ok(adminUserService.create(
                request.username(),
                request.password(),
                request.displayName()
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminUserResult> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateAdminUserRequest request) {
        return ApiResponse.ok(adminUserService.updateDisplayName(id, request.displayName()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AdminUserResult> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeAdminUserStatusRequest request,
                                                     @AuthenticationPrincipal AdminPrincipal principal) {
        Long operatorAdminId = principal == null ? null : principal.getAdminId();
        return ApiResponse.ok(adminUserService.changeStatus(operatorAdminId, id, request.status()));
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<AdminUserResult> resetPassword(@PathVariable Long id,
                                                      @Valid @RequestBody ResetAdminPasswordRequest request) {
        return ApiResponse.ok(adminUserService.resetPassword(id, request.newPassword()));
    }

    @PostMapping("/me/change-password")
    public ApiResponse<Void> changeOwnPassword(@AuthenticationPrincipal AdminPrincipal principal,
                                               @Valid @RequestBody ChangeOwnPasswordRequest request) {
        adminUserService.changeOwnPassword(principal.getAdminId(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok();
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
