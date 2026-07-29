package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/app-platform-policies")
public class AdminAppPlatformPolicyController {

    private final AppPlatformPolicyService service;

    public AdminAppPlatformPolicyController(AppPlatformPolicyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AppPlatformPolicy>> list(@RequestParam String appId) {
        return ApiResponse.ok(service.list(appId));
    }

    @PostMapping
    public ApiResponse<AppPlatformPolicy> upsert(@Valid @RequestBody UpsertRequest request) {
        return ApiResponse.ok(service.upsert(request.appId, request.category, request.enabled,
                request.providerCode, request.configJson, request.credentialJson, request.policyJson));
    }

    public static class UpsertRequest {
        @NotBlank
        public String appId;
        @NotNull
        public PlatformConfigCategory category;
        public boolean enabled = true;
        public String providerCode;
        public String configJson;
        public String credentialJson;
        public String policyJson;
    }
}
