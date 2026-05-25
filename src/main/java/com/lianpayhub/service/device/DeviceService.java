package com.lianpayhub.service.device;

import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchEventType;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.user.BindType;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.LaunchRecordRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.service.app.AppService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceInfoRepository deviceInfoRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final AppService appService;

    public DeviceService(DeviceInfoRepository deviceInfoRepository, LaunchRecordRepository launchRecordRepository,
                         UserInfoRepository userInfoRepository, UserAppBindingRepository userAppBindingRepository,
                         AppService appService) {
        this.deviceInfoRepository = deviceInfoRepository;
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
        launchRecordRepository.save(new LaunchRecord(
                command.appId(),
                device.getId(),
                command.userId(),
                command.platform(),
                command.version(),
                command.networkType(),
                command.ipAddress(),
                LaunchEventType.LAUNCH,
                command.eventData()
        ));
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
}
