package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.ai.AceDataService;
import com.lianpayhub.service.ai.AiProviderAdminService;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ai-platforms")
public class AdminAceDataController {
    private final AceDataService ace;
    private final AiProviderAdminService platforms;
    public AdminAceDataController(AceDataService ace, AiProviderAdminService platforms) { this.ace = ace; this.platforms = platforms; }
    @GetMapping("/acedata/operations")
    public ApiResponse<List<Map<String, Object>>> operations() { return ApiResponse.ok(ace.operations()); }
    @PostMapping("/{id}/acedata/invoke")
    public ApiResponse<Object> invoke(@PathVariable Long id, @Valid @RequestBody InvokeRequest request) {
        return ApiResponse.ok(ace.invoke(platforms.detail(id), request.operation, request.parameters, null));
    }
    public static class InvokeRequest {
        @NotBlank public String operation;
        public Map<String, Object> parameters;
    }
}
