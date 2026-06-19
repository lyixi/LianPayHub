package com.lianpayhub.service.device;

import com.lianpayhub.domain.device.DeviceCodeChangeLog;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchEventType;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.user.BindType;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.repository.DeviceCodeChangeLogRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.LaunchRecordRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.service.app.AppService;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceCodeChangeLogRepository deviceCodeChangeLogRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final AppService appService;

    public DeviceService(DeviceInfoRepository deviceInfoRepository, DeviceCodeChangeLogRepository deviceCodeChangeLogRepository,
                         LaunchRecordRepository launchRecordRepository, UserInfoRepository userInfoRepository,
                         UserAppBindingRepository userAppBindingRepository, AppService appService) {
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceCodeChangeLogRepository = deviceCodeChangeLogRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.userInfoRepository = userInfoRepository;
        this.userAppBindingRepository = userAppBindingRepository;
        this.appService = appService;
    }

    @Transactional
    public DeviceInfo registerOrGet(RegisterDeviceCommand command) {
        appService.requireEnabledApp(command.appId());
        return deviceInfoRepository.findByAppIdAndDeviceCode(command.appId(), command.deviceCode())
                .orElseGet(() -> deviceInfoRepository.save(new DeviceInfo(
                        command.appId(),
                        command.deviceCode(),
                        command.deviceName(),
                        command.deviceType(),
                        command.deviceFingerprint()
                )));
    }

    @Transactional
    public void recordLaunch(RecordLaunchCommand command) {
        DeviceInfo device = registerOrGet(new RegisterDeviceCommand(
                command.appId(),
                command.deviceCode(),
                null,
                null,
                null
        ));
        device.markLaunch();
        if (command.previousSessionEndAt() != null && hasText(command.previousSessionId())) {
            LaunchRecord launch = launchRecordRepository.findFirstByAppIdAndDeviceIdAndSessionIdAndEventTypeOrderByIdDesc(
                    command.appId(), device.getId(), command.previousSessionId().trim(), LaunchEventType.LAUNCH
            ).orElse(null);
            if (launch != null) {
                Long durationSeconds = resolveDurationSeconds(
                        launch.getCreatedAt(),
                        command.previousSessionEndAt(),
                        command.previousDurationSeconds()
                );
                launch.completeSession(command.previousSessionEndAt(), durationSeconds);
                launchRecordRepository.save(launch);
            }
        }
        launchRecordRepository.save(new LaunchRecord(
                command.appId(),
                device.getId(),
                command.userId(),
                command.platform(),
                command.version(),
                command.networkType(),
                command.ipAddress(),
                LaunchEventType.LAUNCH,
                normalizeSessionId(command.sessionId()),
                null,
                null,
                null,
                command.eventData()
        ));
    }

    private Long resolveDurationSeconds(LocalDateTime startAt, LocalDateTime endAt, Long reportedDurationSeconds) {
        if (reportedDurationSeconds != null && reportedDurationSeconds >= 0) {
            return reportedDurationSeconds;
        }
        if (startAt == null || endAt == null || endAt.isBefore(startAt)) {
            return null;
        }
        return Duration.between(startAt, endAt).getSeconds();
    }

    private String normalizeSessionId(String sessionId) {
        return hasText(sessionId) ? sessionId.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Transactional
    public DeviceInfo bindUser(Long id, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId 不能为空");
        }
        DeviceInfo device = deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在"));
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户已被禁用");
        }
        UserAppBinding binding = userAppBindingRepository.findByUserIdAndAppId(userId, device.getAppId())
                .orElse(null);
        if (binding == null) {
            userAppBindingRepository.save(new UserAppBinding(userId, device.getAppId(), BindType.DEVICE_BIND));
        } else if (binding.getStatus() != BindingStatus.ENABLED) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户与 APP 的绑定关系已禁用");
        }
        device.bindUser(userId);
        return deviceInfoRepository.save(device);
    }

    @Transactional
    public DeviceInfo unbind(Long id) {
        DeviceInfo device = deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在"));
        device.unbindUser();
        return deviceInfoRepository.save(device);
    }

    @Transactional
    public DeviceInfo changeDeviceCode(Long id, String deviceCode) {
        return changeDeviceCode(id, deviceCode, null, null, null);
    }

    @Transactional
    public DeviceInfo changeDeviceCode(Long id, String deviceCode, String reason, Long adminId, String adminUsername) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "deviceCode 不能为空");
        }
        String normalizedDeviceCode = deviceCode.trim();
        DeviceInfo device = deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在"));
        DeviceInfo existing = deviceInfoRepository.findByAppIdAndDeviceCode(device.getAppId(), normalizedDeviceCode)
                .orElse(null);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该设备码已绑定到其他设备");
        }
        String oldDeviceCode = device.getDeviceCode();
        if (oldDeviceCode.equals(normalizedDeviceCode)) {
            return device;
        }
        device.changeDeviceCode(normalizedDeviceCode);
        DeviceInfo saved = deviceInfoRepository.save(device);
        deviceCodeChangeLogRepository.save(new DeviceCodeChangeLog(
                id,
                device.getAppId(),
                oldDeviceCode,
                normalizedDeviceCode,
                reason == null ? null : reason.trim(),
                adminId,
                adminUsername
        ));
        return saved;
    }
}
