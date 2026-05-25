package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.device.RegisterDeviceCommand;
import com.lianpayhub.service.member.MemberService;
import com.lianpayhub.service.member.MemberStatusResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final DeviceService deviceService;

    public MemberController(MemberService memberService, DeviceService deviceService) {
        this.memberService = memberService;
        this.deviceService = deviceService;
    }

    @GetMapping("/status")
    public ApiResponse<MemberStatusResult> status(@RequestParam String appId,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long deviceId,
                                                  @RequestParam(required = false) String deviceCode) {
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
}
