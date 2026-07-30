package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.auth.AppAuthService;
import com.lianpayhub.service.auth.AppLoginCommand;
import com.lianpayhub.service.auth.AppLoginResult;
import com.lianpayhub.service.auth.AppPasswordLoginCommand;
import com.lianpayhub.service.auth.SendSmsCodeResult;
import com.lianpayhub.service.auth.SmsCodeService;
import com.lianpayhub.security.AppUserPrincipal;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppAuthService appAuthService;
    private final SmsCodeService smsCodeService;

    public AuthController(AppAuthService appAuthService, SmsCodeService smsCodeService) {
        this.appAuthService = appAuthService;
        this.smsCodeService = smsCodeService;
    }

    @PostMapping("/send-code")
    public ApiResponse<SendSmsCodeResult> sendCode(@Valid @RequestBody SendCodeRequest request,
                                                   HttpServletRequest httpRequest) {
        return ApiResponse.ok(smsCodeService.sendCode(request.appId(), request.mobile(), clientIp(httpRequest)));
    }

    @PostMapping("/login")
    public ApiResponse<AppLoginResult> login(@Valid @RequestBody AppLoginRequest request,
                                             HttpServletRequest httpRequest) {
        return ApiResponse.ok(appAuthService.loginByMobile(new AppLoginCommand(
                request.appId(),
                request.mobile(),
                request.code(),
                request.deviceCode(),
                clientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        )));
    }

    @PostMapping("/password-login")
    public ApiResponse<AppLoginResult> passwordLogin(@Valid @RequestBody AppPasswordLoginRequest request,
                                                     HttpServletRequest httpRequest) {
        return ApiResponse.ok(appAuthService.loginByPassword(new AppPasswordLoginCommand(
                request.appId(),
                request.account(),
                request.password(),
                request.deviceCode(),
                clientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        )));
    }

    @PostMapping("/refresh")
    public ApiResponse<AppLoginResult> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(appAuthService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        appAuthService.logout(request.refreshToken());
        return ApiResponse.ok();
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal AppUserPrincipal principal) {
        appAuthService.logoutAll(principal.getUserId());
        return ApiResponse.ok();
    }

    @PostMapping("/logout-device")
    public ApiResponse<Void> logoutDevice(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @Valid @RequestBody LogoutDeviceRequest request) {
        appAuthService.logoutDevice(principal.getUserId(), principal.getAppId(), request.deviceCode());
        return ApiResponse.ok();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
