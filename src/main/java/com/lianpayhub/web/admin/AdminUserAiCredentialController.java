package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.service.ai.UserAiCredentialAdminService;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user-ai")
public class AdminUserAiCredentialController {
    private final UserAiCredentialAdminService service;

    public AdminUserAiCredentialController(UserAiCredentialAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<UserAiCredential>> list(@RequestParam(required = false) String appId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(appId, PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),100), Sort.by(Sort.Direction.DESC, "id"))));
    }

    @GetMapping("/by-user/{userId}")
    public ApiResponse<java.util.List<UserAiCredential>> byUser(@PathVariable Long userId) {
        return ApiResponse.ok(service.listByUser(userId));
    }

    @PostMapping
    public ApiResponse<UserAiCredential> upsert(@Valid @RequestBody UpsertUserAiRequest request) {
        return ApiResponse.ok(service.upsert(request.userId, request.appId, request.providerCode, request.apiKey, request.quotaUnits));
    }

    public static class UpsertUserAiRequest {
        @NotNull public Long userId;
        @NotBlank public String appId;
        public String providerCode;
        public String apiKey;
        public Long quotaUnits;
    }
}
