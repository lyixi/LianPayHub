package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.DefaultAdminProperties;
import org.springframework.stereotype.Component;

@Component
public class AdminPasswordPolicy {

    private final DefaultAdminProperties defaultAdminProperties;

    public AdminPasswordPolicy(DefaultAdminProperties defaultAdminProperties) {
        this.defaultAdminProperties = defaultAdminProperties;
    }

    public void validate(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码至少需要 6 位");
        }
        if (!defaultAdminProperties.isPasswordComplexityRequired()) {
            return;
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需要包含大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需要包含小写字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需要包含数字");
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需要包含特殊字符");
        }
    }
}
