package com.lianpayhub.bootstrap;

import com.lianpayhub.config.DefaultAdminProperties;
import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.repository.AdminUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private final DefaultAdminProperties properties;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminInitializer(DefaultAdminProperties properties, AdminUserRepository adminUserRepository,
                                   PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUserRepository.existsByUsername(properties.getDefaultUsername())) {
            return;
        }
        adminUserRepository.save(new AdminUser(
                properties.getDefaultUsername(),
                passwordEncoder.encode(properties.getDefaultPassword()),
                "默认管理员"
        ));
    }
}
