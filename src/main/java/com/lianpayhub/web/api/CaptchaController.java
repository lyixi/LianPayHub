package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.captcha.CaptchaChallengeResult;
import com.lianpayhub.service.captcha.CaptchaService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping("/challenge")
    public ApiResponse<CaptchaChallengeResult> challenge(@Valid @RequestBody ChallengeRequest request,
                                                         HttpServletRequest httpRequest) {
        return ApiResponse.ok(captchaService.createChallenge(request.appId, request.purpose, clientIp(httpRequest)));
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verify(@Valid @RequestBody VerifyRequest request) {
        captchaService.verifyAndConsume(request.appId, request.purpose, request.token, request.code);
        return ApiResponse.ok();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static class ChallengeRequest {
        @NotBlank
        public String appId;
        public String purpose;
    }

    public static class VerifyRequest {
        @NotBlank
        public String appId;
        public String purpose;
        @NotBlank
        public String token;
        @NotBlank
        public String code;
    }
}
