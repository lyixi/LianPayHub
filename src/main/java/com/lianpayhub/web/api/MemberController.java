package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.device.RegisterDeviceCommand;
import com.lianpayhub.service.member.MemberService;
import com.lianpayhub.service.member.MemberStatusResult;
import com.lianpayhub.service.rate.RateLimitService;
import com.lianpayhub.service.security.AppUserAccessService;
import java.time.Duration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final DeviceService deviceService;
    private final RateLimitService rateLimitService;
    private final AppUserAccessService appUserAccessService;

    public MemberController(MemberService memberService, DeviceService deviceService,
                            RateLimitService rateLimitService, AppUserAccessService appUserAccessService) {
        this.memberService = memberService;
        this.deviceService = deviceService;
        this.rateLimitService = rateLimitService;
        this.appUserAccessService = appUserAccessService;
    }

    @GetMapping("/status")
    public ApiResponse<MemberStatusResult> status(@RequestParam String appId,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long deviceId,
                                                  @RequestParam(required = false) String deviceCode,
                                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        rateLimitService.requireWithinLimit("member:status:" + appId + ":" + subjectKey(userId, deviceId, deviceCode),
                120, Duration.ofMinutes(1));
        if (userId != null) {
            appUserAccessService.requireUserAccessWhenNeeded(appId, userId, principal);
        }
        if (deviceId == null && deviceCode != null && !deviceCode.trim().isEmpty()) {
            DeviceInfo device = deviceService.registerOrGet(new RegisterDeviceCommand(
                    appId,
                    deviceCode,
                    null,
                    null,
                    null
            ));
            deviceId = device.getId();
        }
        if (deviceId != null) {
            return ApiResponse.ok(memberService.getDeviceMemberStatus(appId, deviceId));
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId 和 deviceId 至少提供一个");
        }
        return ApiResponse.ok(memberService.getUserMemberStatus(appId, userId));
    }

    private String subjectKey(Long userId, Long deviceId, String deviceCode) {
        if (deviceId != null) {
            return "device-id:" + deviceId;
        }
        if (deviceCode != null && !deviceCode.trim().isEmpty()) {
            return "device-code:" + deviceCode.trim();
        }
        if (userId != null) {
            return "user:" + userId;
        }
        return "anonymous";
    }
}
