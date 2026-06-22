package com.lianpayhub.service.app;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppStatus;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.service.security.AppSecretService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppService {

    private final AppInfoRepository appInfoRepository;
    private final AppSecretService appSecretService;
    private final PackageInfoRepository packageInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    public AppService(AppInfoRepository appInfoRepository, AppSecretService appSecretService,
                      PackageInfoRepository packageInfoRepository, PaymentOrderRepository paymentOrderRepository,
                      UserAppBindingRepository userAppBindingRepository, DeviceInfoRepository deviceInfoRepository) {
        this.appInfoRepository = appInfoRepository;
        this.appSecretService = appSecretService;
        this.packageInfoRepository = packageInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.userAppBindingRepository = userAppBindingRepository;
        this.deviceInfoRepository = deviceInfoRepository;
    }

    @Transactional
    public CreateAppResult createApp(CreateAppCommand command) {
        if (appInfoRepository.existsByAppId(command.appId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "appId 已存在");
        }

        String secret = appSecretService.generateSecret();
        AppInfo appInfo = new AppInfo(
                command.appId(),
                command.appName(),
                appSecretService.hashSecret(secret),
                command.appType(),
                command.needMobileLogin(),
                command.needDeviceVip()
        );
        AppInfo saved = appInfoRepository.save(appInfo);
        return new CreateAppResult(saved.getId(), saved.getAppId(), secret);
    }

    @Transactional
    public AppInfo updateApp(Long id, UpdateAppCommand command) {
        AppInfo appInfo = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        appInfo.update(command.appName(), command.needMobileLogin(), command.needDeviceVip());
        return appInfo;
    }

    @Transactional
    public AppInfo changeStatus(Long id, ChangeAppStatusCommand command) {
        AppInfo appInfo = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        appInfo.changeStatus(command.status());
        return appInfo;
    }

    @Transactional
    public ResetAppSecretResult resetSecret(Long id) {
        AppInfo appInfo = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        String secret = appSecretService.generateSecret();
        appInfo.resetSecret(appSecretService.hashSecret(secret));
        return new ResetAppSecretResult(appInfo.getId(), appInfo.getAppId(), appInfo.getAppSecretVersion(), secret);
    }

    @Transactional
    public void deleteApp(Long id) {
        AppInfo appInfo = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        String appId = appInfo.getAppId();
        if (packageInfoRepository.countByAppId(appId) > 0
                || paymentOrderRepository.countByAppId(appId) > 0
                || userAppBindingRepository.countByAppId(appId) > 0
                || deviceInfoRepository.countByAppId(appId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP 下已有套餐、订单、绑定或设备数据，暂不允许删除");
        }
        appInfoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AppInfo requireApp(String appId) {
        return appInfoRepository.findByAppId(appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
    }

    @Transactional(readOnly = true)
    public AppInfo requireEnabledApp(String appId) {
        AppInfo appInfo = requireApp(appId);
        if (appInfo.getStatus() != AppStatus.ENABLED) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP 已停用");
        }
        return appInfo;
    }
}
