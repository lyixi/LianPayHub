package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.DefaultAdminProperties;
import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.repository.AdminUserRepository;
import com.lianpayhub.service.security.JwtService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 10;

    private final Map<String, LoginFailure> loginFailures = new ConcurrentHashMap<>();

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DefaultAdminProperties defaultAdminProperties;

    public AdminAuthService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder,
                            JwtService jwtService, DefaultAdminProperties defaultAdminProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.defaultAdminProperties = defaultAdminProperties;
    }

    @Transactional
    public AdminLoginResult login(String username, String password) {
        String normalizedUsername = username == null ? "" : username.trim();
        checkLoginLock(normalizedUsername);
        AdminUser adminUser = adminUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> {
                    recordLoginFailure(normalizedUsername);
                    return new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
                });
        if (!adminUser.isEnabled() || !passwordEncoder.matches(password, adminUser.getPasswordHash())) {
            recordLoginFailure(normalizedUsername);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }
        loginFailures.remove(normalizedUsername);
        adminUser.markLogin();
        String token = jwtService.generateAdminToken(adminUser.getId(), adminUser.getUsername(), adminUser.getPasswordVersion());
        boolean mustChangePassword = defaultAdminProperties.isForceDefaultPasswordChange()
                && defaultAdminProperties.getDefaultUsername().equals(adminUser.getUsername())
                && passwordEncoder.matches(defaultAdminProperties.getDefaultPassword(), adminUser.getPasswordHash());
        return new AdminLoginResult(token, adminUser.getId(), adminUser.getUsername(), adminUser.getDisplayName(), mustChangePassword);
    }

    private void checkLoginLock(String username) {
        LoginFailure failure = loginFailures.get(username);
        if (failure == null || failure.lockedUntil == null) {
            return;
        }
        if (failure.lockedUntil.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "登录失败次数过多，请稍后再试");
        }
        loginFailures.remove(username);
    }

    private void recordLoginFailure(String username) {
        loginFailures.compute(username, (key, old) -> {
            LoginFailure failure = old == null ? new LoginFailure() : old;
            failure.count++;
            if (failure.count >= MAX_FAILED_ATTEMPTS) {
                failure.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            }
            return failure;
        });
    }

    private static class LoginFailure {
        private int count;
        private LocalDateTime lockedUntil;
    }
}
