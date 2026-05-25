package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.device.RegisterDeviceCommand;
import com.lianpayhub.service.member.MemberService;
import com.lianpayhub.service.member.MemberStatusResult;
import com.lianpayhub.service.rate.RateLimitService;
import java.time.Duration;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final DeviceService deviceService;
    private final RateLimitService rateLimitService;

    public MemberController(MemberService memberService, DeviceService deviceService,
                            RateLimitService rateLimitService) {
        this.memberService = memberService;
        this.deviceService = deviceService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/status")
    public ApiResponse<MemberStatusResult> status(@RequestParam String appId,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long deviceId,
                                                  @RequestParam(required = false) String deviceCode) {
        rateLimitService.requireWithinLimit("member:status:" + appId + ":" + subjectKey(userId, deviceId, deviceCode),
                120, Duration.ofMinutes(1));
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
