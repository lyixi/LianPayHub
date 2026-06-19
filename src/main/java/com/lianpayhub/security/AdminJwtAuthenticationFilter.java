package com.lianpayhub.security;

import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.repository.AdminUserRepository;
import com.lianpayhub.service.security.JwtService;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminUserRepository adminUserRepository;

    public AdminJwtAuthenticationFilter(JwtService jwtService, AdminUserRepository adminUserRepository) {
        this.jwtService = jwtService;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean authenticated = authenticate(token);
            if (!authenticated && isProtectedAdminPath(request)) {
                writeUnauthorized(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private boolean isProtectedAdminPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/admin/") && !uri.startsWith("/admin/auth/login");
    }

    private boolean authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            if (!"ADMIN".equals(claims.get("type", String.class))) {
                return false;
            }
            Long adminId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            Integer tokenPasswordVersion = claims.get("passwordVersion", Integer.class);
            AdminUser adminUser = adminUserRepository.findById(adminId).orElse(null);
            if (adminUser == null || !adminUser.isEnabled()) {
                return false;
            }
            if (tokenPasswordVersion == null || !tokenPasswordVersion.equals(adminUser.getPasswordVersion())) {
                return false;
            }
            AdminPrincipal principal = new AdminPrincipal(adminId, username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
    }
}
