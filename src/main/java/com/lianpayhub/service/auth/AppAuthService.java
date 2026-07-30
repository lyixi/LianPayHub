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
import com.lianpayhub.service.ai.UserAiProvisionService;
import com.lianpayhub.service.app.AppService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.time.Duration;

@Service
public class AppAuthService {

    private static final int MAX_PASSWORD_FAILURES = 5;
    private static final Duration PASSWORD_LOCK_DURATION = Duration.ofMinutes(15);

    private final AppService appService;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository bindingRepository;
    private final AppLoginLogRepository loginLogRepository;
    private final SecurityProperties securityProperties;
    private final SmsCodeService smsCodeService;
    private final UserAiProvisionService userAiProvisionService;
    private final PasswordEncoder passwordEncoder;
    private final UserRefreshTokenService refreshTokenService;

    public AppAuthService(AppService appService, UserInfoRepository userInfoRepository,
                          UserAppBindingRepository bindingRepository, AppLoginLogRepository loginLogRepository,
                          SecurityProperties securityProperties,
                          SmsCodeService smsCodeService,
                          UserAiProvisionService userAiProvisionService,
                          PasswordEncoder passwordEncoder,
                          UserRefreshTokenService refreshTokenService) {
        this.appService = appService;
        this.userInfoRepository = userInfoRepository;
        this.bindingRepository = bindingRepository;
        this.loginLogRepository = loginLogRepository;
        this.securityProperties = securityProperties;
        this.smsCodeService = smsCodeService;
        this.userAiProvisionService = userAiProvisionService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AppLoginResult loginByMobile(AppLoginCommand command) {
        Long userId = null;
        try {
            AppInfo appInfo = appService.requireEnabledApp(command.appId());
            if (appInfo.getAppType() == AppType.DEVICE_ONLY || !appInfo.isNeedMobileLogin()) {
                throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 不支持手机号登录");
            }
            if (Boolean.TRUE.equals(securityProperties.getSmsCodeRequired())) {
                smsCodeService.verifyAndConsume(command.appId(), command.mobile(), command.code());
            }

            UserInfo userInfo = userInfoRepository.findByMobile(command.mobile())
                    .orElseGet(() -> userInfoRepository.save(new UserInfo(command.mobile())));
            userId = userInfo.getId();
            if (userInfo.isLoginBlocked() || userInfo.isTemporarilyLocked()) {
                throw new BusinessException(ErrorCode.CONFLICT, "用户已被禁用或已锁定");
            }

            if (!bindingRepository.existsByUserIdAndAppId(userInfo.getId(), command.appId())) {
                bindingRepository.save(new UserAppBinding(userInfo.getId(), command.appId(), BindType.MOBILE_LOGIN));
            }
            userAiProvisionService.ensureCredentialForBoundUser(userInfo.getId(), command.appId());
            userInfo.markLogin();

            UserRefreshTokenService.IssuedTokens tokens = refreshTokenService.issue(
                    userInfo, appInfo, command.deviceCode(), command.ipAddress(), command.userAgent());
            loginLogRepository.save(new AppLoginLog(command.appId(), userInfo.getId(), userInfo.getMobile(),
                    AppLoginType.MOBILE, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.SUCCESS, null));
            userInfoRepository.save(userInfo);
            return loginResult(tokens, userInfo, command.appId());
        } catch (RuntimeException ex) {
            loginLogRepository.save(new AppLoginLog(command.appId(), userId, command.mobile(),
                    AppLoginType.MOBILE, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.FAILED, ex.getMessage()));
            throw ex;
        }
    }

    @Transactional
    public AppLoginResult loginByPassword(AppPasswordLoginCommand command) {
        Long userId = null;
        String mobile = null;
        try {
            AppInfo appInfo = appService.requireEnabledApp(command.appId());
            if (!appInfo.isAllowPasswordLogin()) {
                throw new BusinessException(ErrorCode.CONFLICT, "当前 APP 未开启密码登录");
            }

            String account = normalize(command.account());
            Optional<UserInfo> found = userInfoRepository.findByUsername(account);
            if (!found.isPresent()) {
                found = userInfoRepository.findByMobile(account);
            }
            UserInfo userInfo = found
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "账号或密码错误"));
            userId = userInfo.getId();
            mobile = userInfo.getMobile();
            if (userInfo.isLoginBlocked() || userInfo.isTemporarilyLocked()) {
                throw new BusinessException(ErrorCode.CONFLICT, "用户已被禁用或已锁定");
            }
            if (userInfo.getPasswordHash() == null || !passwordEncoder.matches(command.password(), userInfo.getPasswordHash())) {
                userInfo.recordFailedPasswordAttempt(MAX_PASSWORD_FAILURES, PASSWORD_LOCK_DURATION);
                userInfoRepository.save(userInfo);
                throw new BusinessException(ErrorCode.BAD_REQUEST, "账号或密码错误");
            }

            if (!bindingRepository.existsByUserIdAndAppId(userInfo.getId(), command.appId())) {
                bindingRepository.save(new UserAppBinding(userInfo.getId(), command.appId(), BindType.MOBILE_LOGIN));
            }
            userAiProvisionService.ensureCredentialForBoundUser(userInfo.getId(), command.appId());
            userInfo.markLogin();
            userInfo.resetPasswordFailures();

            UserRefreshTokenService.IssuedTokens tokens = refreshTokenService.issue(
                    userInfo, appInfo, command.deviceCode(), command.ipAddress(), command.userAgent());
            loginLogRepository.save(new AppLoginLog(command.appId(), userInfo.getId(), userInfo.getMobile(),
                    AppLoginType.PASSWORD, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.SUCCESS, null));
            userInfoRepository.save(userInfo);
            return loginResult(tokens, userInfo, command.appId());
        } catch (RuntimeException ex) {
            loginLogRepository.save(new AppLoginLog(command.appId(), userId, mobile,
                    AppLoginType.PASSWORD, null, null, command.ipAddress(), command.userAgent(),
                    LogResultStatus.FAILED, ex.getMessage()));
            throw ex;
        }
    }

    @Transactional
    public AppLoginResult refresh(String refreshToken) {
        UserRefreshTokenService.IssuedTokens tokens = refreshTokenService.refresh(refreshToken);
        UserInfo userInfo = userInfoRepository.findById(tokens.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "用户不存在"));
        return loginResult(tokens, userInfo, tokens.getAppId());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeByToken(refreshToken, "logout");
    }

    @Transactional
    public void logoutAll(Long userId) {
        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        userInfo.bumpTokenVersion();
        userInfoRepository.save(userInfo);
        refreshTokenService.revokeByUser(userId, "logout_all");
    }

    @Transactional
    public void logoutDevice(Long userId, String appId, String deviceCode) {
        refreshTokenService.revokeByUserAndDevice(userId, appId, deviceCode, "logout_device");
    }

    private AppLoginResult loginResult(UserRefreshTokenService.IssuedTokens tokens, UserInfo userInfo, String appId) {
        return new AppLoginResult(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                userInfo.getId(),
                userInfo.getMobile(),
                appId,
                userInfo.isMustChangePassword(),
                tokens.getAccessTokenExpiresInMinutes(),
                tokens.getRefreshTokenExpiresInMinutes()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
