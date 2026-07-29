package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.ai.AiChatResult;
import com.lianpayhub.service.ai.AiGatewayService;
import com.lianpayhub.service.security.AppUserAccessService;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiGatewayController {
    private final AiGatewayService aiGatewayService;
    private final AppUserAccessService appUserAccessService;

    public AiGatewayController(AiGatewayService aiGatewayService, AppUserAccessService appUserAccessService) {
        this.aiGatewayService = aiGatewayService;
        this.appUserAccessService = appUserAccessService;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResult> chat(@Valid @RequestBody AiChatRequest request,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        appUserAccessService.requireUserAccessWhenNeeded(request.appId(), principal == null ? null : principal.getUserId(), principal);
        Long userId = principal == null ? null : principal.getUserId();
        return ApiResponse.ok(aiGatewayService.chat(userId, request.appId(), request.providerCode(), request.model(), request.message(), request.stream(), request.imageUrl()));
    }
}
