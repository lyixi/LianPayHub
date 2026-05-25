package com.lianpayhub.service.auth;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppType;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.log.AppLoginType;
import com.lianpayhub.domain.log.LogResultStatus;
import com.lianpayhub.domain.user.BindType;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.AppLoginLogRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppAuthService {

    private final AppService appService;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository bindingRepository;
    private final AppLoginLogRepository loginLogRepository;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;

    public AppAuthService(AppService appService, UserInfoRepository userInfoRepository,
                          UserAppBindingRepository bindingRepository, AppLoginLogRepository loginLogRepository,
                          JwtService jwtService,
                          SecurityProperties securityProperties) {
        this.appService = appService;
        this.userInfoRepository = userInfoRepository;
        this.bindingRepository = bindingRepository;
        this.loginLogRepository = loginLogRepository;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public AppLoginResult loginByMobile(AppLoginCommand command) {
        Long userId = null;
        try {
            if (Boolean.TRUE.equals(securityProperties.getSmsCodeRequired())
                    && (command.code() == null || command.code().trim().isEmpty())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不能为空");
            }
            AppInfo appInfo = appService.requireEnabledApp(command.appId());
            if (appInfo.getAppType() == AppType.DEVICE_ONLY || !appInfo.isNeedMobileLogin()) {
                throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 不支持手机号登录");
            }

            UserInfo userInfo = userInfoRepository.findByMobile(command.mobile())
                    .orElseGet(() -> userInfoRepository.save(new UserInfo(command.mobile())));
            userId = userInfo.getId();
            if (!userInfo.isEnabled()) {
                throw new BusinessException(ErrorCode.CONFLICT, "用户已被禁用");
            }

            if (!bindingRepository.existsByUserIdAndAppId(userInfo.getId(), command.appId())) {
                bindingRepository.save(new UserAppBinding(userInfo.getId(), command.appId(), BindType.MOBILE_LOGIN));
            }

            String token = jwtService.generateUserToken(userInfo.getId(), command.appId(), userInfo.getMobile());
            loginLogRepository.save(new AppLoginLog(command.appId(), userInfo.getId(), userInfo.getMobile(),
                    AppLoginType.MOBILE, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.SUCCESS, null));
            return new AppLoginResult(token, userInfo.getId(), userInfo.getMobile(), command.appId());
        } catch (RuntimeException ex) {
            loginLogRepository.save(new AppLoginLog(command.appId(), userId, command.mobile(),
                    AppLoginType.MOBILE, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.FAILED, ex.getMessage()));
            throw ex;
        }
    }
}
