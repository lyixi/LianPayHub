package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.auth.AppAuthService;
import com.lianpayhub.service.auth.AppLoginCommand;
import com.lianpayhub.service.auth.AppLoginResult;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppAuthService appAuthService;

    public AuthController(AppAuthService appAuthService) {
        this.appAuthService = appAuthService;
    }

    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        // 当前阶段不接短信服务，先保留接口形态，方便 APP 侧流程联调。
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<AppLoginResult> login(@Valid @RequestBody AppLoginRequest request,
                                             HttpServletRequest httpRequest) {
        return ApiResponse.ok(appAuthService.loginByMobile(new AppLoginCommand(
                request.appId(),
                request.mobile(),
                request.code(),
                clientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        )));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
