package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.service.ai.AiProviderAdminService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ai-platforms")
public class AdminAiPlatformController {
    private final AiProviderAdminService service;

    public AdminAiPlatformController(AiProviderAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AiProviderConfig>> list() { return ApiResponse.ok(service.list()); }

    @GetMapping("/{id}")
    public ApiResponse<AiProviderConfig> detail(@PathVariable Long id) { return ApiResponse.ok(service.detail(id)); }

    @PostMapping
    public ApiResponse<AiProviderConfig> create(@Valid @RequestBody UpsertAiPlatformRequest request) {
        return ApiResponse.ok(service.create(request.providerCode, request.displayName, request.baseUrl, request.consoleBaseUrl, request.configJson, request.credentialJson));
    }

    @PutMapping("/{id}")
    public ApiResponse<AiProviderConfig> update(@PathVariable Long id, @Valid @RequestBody UpsertAiPlatformRequest request) {
        return ApiResponse.ok(service.update(id, request.displayName, request.baseUrl, request.consoleBaseUrl, request.configJson, request.credentialJson));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AiProviderConfig> status(@PathVariable Long id, @RequestBody StatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.enabled));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/moacode/pricing")
    public ApiResponse<java.util.Map<String, Object>> moacodePricing(@PathVariable Long id) {
        return ApiResponse.ok(service.moacodePricing(id));
    }

    @GetMapping("/{id}/moacode/usage")
    public ApiResponse<java.util.Map<String, Object>> moacodeUsage(@PathVariable Long id) {
        return ApiResponse.ok(service.moacodeUsage(id));
    }

    @GetMapping("/{id}/account-balance")
    public ApiResponse<java.util.Map<String, Object>> accountBalance(@PathVariable Long id) {
        return ApiResponse.ok(service.accountBalance(id));
    }

    public static class UpsertAiPlatformRequest {
        @NotBlank public String providerCode;
        @NotBlank public String displayName;
        public String baseUrl;
        public String consoleBaseUrl;
        public String configJson;
        public String credentialJson;
    }
    public static class StatusRequest { public boolean enabled; }
}
