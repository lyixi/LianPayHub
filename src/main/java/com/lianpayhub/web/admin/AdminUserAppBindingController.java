package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.service.user.UserAppBindingService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user-bindings")
public class AdminUserAppBindingController {

    private final UserAppBindingService bindingService;

    public AdminUserAppBindingController(UserAppBindingService bindingService) {
        this.bindingService = bindingService;
    }

    @GetMapping
    public ApiResponse<Page<UserAppBinding>> list(@RequestParam(required = false) String appId,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) BindingStatus status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(bindingService.search(appId, userId, status, pageRequest(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAppBinding> detail(@PathVariable Long id) {
        return ApiResponse.ok(bindingService.detail(id));
    }

    @PostMapping
    public ApiResponse<UserAppBinding> create(@Valid @RequestBody CreateUserAppBindingRequest request) {
        return ApiResponse.ok(bindingService.create(request.userId(), request.appId(), request.bindType()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserAppBinding> changeStatus(@PathVariable Long id,
                                                    @Valid @RequestBody ChangeUserAppBindingStatusRequest request) {
        return ApiResponse.ok(bindingService.changeStatus(id, request.status()));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
