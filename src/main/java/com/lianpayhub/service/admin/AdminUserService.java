package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.domain.admin.AdminUserStatus;
import com.lianpayhub.repository.AdminUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPasswordPolicy passwordPolicy;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder,
                            AdminPasswordPolicy passwordPolicy) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResult> list(String username, PageRequest pageRequest) {
        Page<AdminUser> result = username == null || username.trim().isEmpty()
                ? adminUserRepository.findAll(pageRequest)
                : adminUserRepository.findByUsernameContaining(username.trim(), pageRequest);
        return result.map(AdminUserResult::new);
    }

    @Transactional(readOnly = true)
    public AdminUserResult detail(Long id) {
        return new AdminUserResult(requireAdmin(id));
    }

    @Transactional
    public AdminUserResult create(String username, String password, String displayName) {
        String normalizedUsername = normalizeUsername(username);
        if (adminUserRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessException(ErrorCode.CONFLICT, "管理员用户名已存在");
        }
        passwordPolicy.validate(password);
        AdminUser adminUser = new AdminUser(
                normalizedUsername,
                passwordEncoder.encode(password),
                normalizeDisplayName(displayName, normalizedUsername)
        );
        return new AdminUserResult(adminUserRepository.save(adminUser));
    }

    @Transactional
    public AdminUserResult updateDisplayName(Long id, String displayName) {
        AdminUser adminUser = requireAdmin(id);
        adminUser.changeDisplayName(normalizeDisplayName(displayName, adminUser.getUsername()));
        return new AdminUserResult(adminUserRepository.save(adminUser));
    }

    @Transactional
    public AdminUserResult changeStatus(Long operatorAdminId, Long id, AdminUserStatus status) {
        AdminUser adminUser = requireAdmin(id);
        if (operatorAdminId != null && operatorAdminId.equals(id) && status == AdminUserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能停用当前登录管理员");
        }
        if (status == AdminUserStatus.DISABLED
                && adminUser.getStatus() == AdminUserStatus.ENABLED
                && adminUserRepository.countByStatus(AdminUserStatus.ENABLED) <= 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "至少需要保留一个启用管理员");
        }
        adminUser.changeStatus(status);
        return new AdminUserResult(adminUserRepository.save(adminUser));
    }

    @Transactional
    public AdminUserResult resetPassword(Long id, String newPassword) {
        passwordPolicy.validate(newPassword);
        AdminUser adminUser = requireAdmin(id);
        adminUser.resetPassword(passwordEncoder.encode(newPassword));
        return new AdminUserResult(adminUserRepository.save(adminUser));
    }

    @Transactional
    public void changeOwnPassword(Long adminId, String oldPassword, String newPassword) {
        passwordPolicy.validate(newPassword);
        AdminUser adminUser = requireAdmin(adminId);
        if (!passwordEncoder.matches(oldPassword, adminUser.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码不正确");
        }
        adminUser.resetPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(adminUser);
    }

    private AdminUser requireAdmin(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在"));
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String normalizeDisplayName(String displayName, String fallback) {
        String value = displayName == null ? "" : displayName.trim();
        return value.isEmpty() ? fallback : value;
    }
}
