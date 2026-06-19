package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.device.RecordLaunchCommand;
import com.lianpayhub.service.device.RegisterDeviceCommand;
import com.lianpayhub.service.rate.RateLimitService;
import javax.validation.Valid;
import java.time.Duration;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;
    private final RateLimitService rateLimitService;

    public DeviceController(DeviceService deviceService, RateLimitService rateLimitService) {
        this.deviceService = deviceService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ApiResponse<DeviceInfo> register(@Valid @RequestBody RegisterDeviceRequest request) {
        rateLimitService.requireWithinLimit("device:register:" + request.appId() + ":" + request.deviceCode(),
                30, Duration.ofMinutes(1));
        return ApiResponse.ok(deviceService.registerOrGet(new RegisterDeviceCommand(
                request.appId(),
                request.deviceCode(),
                request.deviceName(),
                request.deviceType(),
                request.deviceFingerprint()
        )));
    }

    @PostMapping("/launch")
    public ApiResponse<Void> launch(@Valid @RequestBody LaunchRequest request) {
        rateLimitService.requireWithinLimit("device:launch:" + request.appId() + ":" + request.deviceCode(),
                120, Duration.ofMinutes(1));
        deviceService.recordLaunch(new RecordLaunchCommand(
                request.appId(),
                request.deviceCode(),
                request.userId(),
                request.platform(),
                request.version(),
                request.networkType(),
                request.ipAddress(),
                request.sessionId(),
                request.previousSessionId(),
                request.previousSessionStartAt(),
                request.previousSessionEndAt(),
                request.previousDurationSeconds(),
                request.eventData()
        ));
        return ApiResponse.ok();
    }
}
