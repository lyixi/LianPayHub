package com.lianpayhub.service.security;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppType;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.app.AppService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserAccessService {

    private final SecurityProperties securityProperties;
    private final AppService appService;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository bindingRepository;

    public AppUserAccessService(SecurityProperties securityProperties,
                                AppService appService,
                                UserInfoRepository userInfoRepository,
                                UserAppBindingRepository bindingRepository) {
        this.securityProperties = securityProperties;
        this.appService = appService;
        this.userInfoRepository = userInfoRepository;
        this.bindingRepository = bindingRepository;
    }

    @Transactional(readOnly = true)
    public void requireUserAccessWhenNeeded(String appId, Long userId, AppUserPrincipal principal) {
        AppInfo appInfo = appService.requireEnabledApp(appId);
        if (appInfo.getAppType() == AppType.DEVICE_ONLY) {
            return;
        }
        if (!Boolean.TRUE.equals(securityProperties.getApiAuthEnabled())) {
            return;
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "标准 APP 接口必须提供 userId");
        }
        if (principal == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "请先登录");
        }
        if (!appId.equals(principal.getAppId()) || !userId.equals(principal.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "登录用户与请求用户不匹配");
        }

        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (!userInfo.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户已被禁用");
        }
        UserAppBinding binding = bindingRepository.findByUserIdAndAppId(userId, appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "用户未绑定该 APP"));
        if (binding.getStatus() != BindingStatus.ENABLED) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户与 APP 的绑定关系已禁用");
        }
    }
}
