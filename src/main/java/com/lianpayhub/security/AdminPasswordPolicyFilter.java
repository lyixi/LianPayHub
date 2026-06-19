package com.lianpayhub.security;

import com.lianpayhub.config.DefaultAdminProperties;
import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.repository.AdminUserRepository;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminPasswordPolicyFilter extends OncePerRequestFilter {

    private final AdminUserRepository adminUserRepository;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;
    private final DefaultAdminProperties defaultAdminProperties;

    public AdminPasswordPolicyFilter(AdminUserRepository adminUserRepository, ObjectProvider<PasswordEncoder> passwordEncoderProvider,
                                     DefaultAdminProperties defaultAdminProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoderProvider = passwordEncoderProvider;
        this.defaultAdminProperties = defaultAdminProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/admin/")
                || uri.startsWith("/admin/auth/login")
                || uri.startsWith("/admin/admin-users/me/change-password");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AdminPrincipal principal = principal();
        if (defaultAdminProperties.isForceDefaultPasswordChange() && principal != null && mustChangePassword(principal)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"默认管理员密码必须先修改\",\"data\":{\"mustChangePassword\":true}}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private AdminPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal)) {
            return null;
        }
        return (AdminPrincipal) authentication.getPrincipal();
    }

    private boolean mustChangePassword(AdminPrincipal principal) {
        if (!defaultAdminProperties.getDefaultUsername().equals(principal.getUsername())) {
            return false;
        }
        AdminUser adminUser = adminUserRepository.findById(principal.getAdminId()).orElse(null);
        return adminUser != null
                && passwordEncoderProvider.getObject().matches(defaultAdminProperties.getDefaultPassword(), adminUser.getPasswordHash());
    }
}
