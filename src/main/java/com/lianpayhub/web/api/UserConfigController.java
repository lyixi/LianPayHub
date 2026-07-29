package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.config.UserConfigResult;
import com.lianpayhub.service.config.UserConfigService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configs")
public class UserConfigController {

    private final UserConfigService userConfigService;

    public UserConfigController(UserConfigService userConfigService) {
        this.userConfigService = userConfigService;
    }

    @GetMapping
    public ApiResponse<List<UserConfigResult>> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.ok(userConfigService.list(principal.getUserId(), principal.getAppId()));
    }

    @GetMapping("/changes")
    public ApiResponse<List<UserConfigResult>> changes(@AuthenticationPrincipal AppUserPrincipal principal,
                                                       @RequestParam(defaultValue = "0") Long sinceVersion) {
        return ApiResponse.ok(userConfigService.changes(principal.getUserId(), principal.getAppId(), sinceVersion));
    }

    @GetMapping("/{key}")
    public ApiResponse<UserConfigResult> get(@AuthenticationPrincipal AppUserPrincipal principal,
                                             @PathVariable String key) {
        return ApiResponse.ok(userConfigService.get(principal.getUserId(), principal.getAppId(), key));
    }

    @PutMapping("/{key}")
    public ApiResponse<UserConfigResult> put(@AuthenticationPrincipal AppUserPrincipal principal,
                                             @PathVariable String key,
                                             @RequestBody UpsertUserConfigRequest request) {
        return ApiResponse.ok(userConfigService.put(
                principal.getUserId(),
                principal.getAppId(),
                key,
                request.contentType(),
                request.contentText()
        ));
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable String key) {
        userConfigService.delete(principal.getUserId(), principal.getAppId(), key);
        return ApiResponse.ok();
    }
}
