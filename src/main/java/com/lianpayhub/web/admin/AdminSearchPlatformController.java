package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.search.SearchPlatformConfig;
import com.lianpayhub.service.search.SearchPlatformAdminService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/search-platforms")
public class AdminSearchPlatformController {

    private final SearchPlatformAdminService service;

    public AdminSearchPlatformController(SearchPlatformAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SearchPlatformConfig>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<SearchPlatformConfig> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping
    public ApiResponse<SearchPlatformConfig> create(@Valid @RequestBody UpsertRequest request) {
        return ApiResponse.ok(service.create(request.providerCode, request.displayName, request.baseUrl,
                request.consoleBaseUrl, request.configJson, request.credentialJson));
    }

    @PutMapping("/{id}")
    public ApiResponse<SearchPlatformConfig> update(@PathVariable Long id, @Valid @RequestBody UpsertRequest request) {
        return ApiResponse.ok(service.update(id, request.displayName, request.baseUrl, request.consoleBaseUrl,
                request.configJson, request.credentialJson));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<SearchPlatformConfig> status(@PathVariable Long id, @RequestBody StatusRequest request) {
        return ApiResponse.ok(service.changeEnabled(id, request.enabled));
    }

    public static class UpsertRequest {
        @NotBlank
        public String providerCode;
        @NotBlank
        public String displayName;
        public String baseUrl;
        public String consoleBaseUrl;
        public String configJson;
        public String credentialJson;
    }

    public static class StatusRequest {
        public boolean enabled;
    }
}
