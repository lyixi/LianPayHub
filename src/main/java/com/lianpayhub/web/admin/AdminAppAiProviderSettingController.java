package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.service.ai.AppAiProviderSettingAdminService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/app-ai-providers")
public class AdminAppAiProviderSettingController {
    private final AppAiProviderSettingAdminService service;

    public AdminAppAiProviderSettingController(AppAiProviderSettingAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AppAiProviderSetting>> list(@RequestParam String appId) {
        return ApiResponse.ok(service.list(appId));
    }

    @PostMapping
    public ApiResponse<AppAiProviderSetting> upsert(@Valid @RequestBody UpsertRequest request) {
        return ApiResponse.ok(service.upsert(request.appId, request.providerCode, request.enabled,
                request.autoProvisionUserKey, request.defaultQuotaUnits, request.dailyLimitUnits, request.keyGroupId));
    }

    public static class UpsertRequest {
        @NotBlank public String appId;
        @NotBlank public String providerCode;
        public boolean enabled;
        public boolean autoProvisionUserKey;
        public Long defaultQuotaUnits;
        public Long dailyLimitUnits;
        public String keyGroupId;
    }
}
