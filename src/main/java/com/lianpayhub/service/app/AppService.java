package com.lianpayhub.service.app;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppStatus;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.service.security.AppSecretService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppService {

    private final AppInfoRepository appInfoRepository;
    private final AppSecretService appSecretService;

    public AppService(AppInfoRepository appInfoRepository, AppSecretService appSecretService) {
        this.appInfoRepository = appInfoRepository;
        this.appSecretService = appSecretService;
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
