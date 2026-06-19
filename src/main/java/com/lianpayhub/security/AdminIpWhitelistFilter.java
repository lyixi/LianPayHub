package com.lianpayhub.security;

import com.lianpayhub.config.SecurityProperties;
import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminIpWhitelistFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    public AdminIpWhitelistFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/admin/") || securityProperties.getAdminIpWhitelist().isEmpty();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = clientIp(request);
        if (!allowed(clientIp, securityProperties.getAdminIpWhitelist())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"当前 IP 不允许访问管理后台\",\"data\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean allowed(String clientIp, List<String> whitelist) {
        for (String item : whitelist) {
            String rule = item == null ? "" : item.trim();
            if (rule.isEmpty()) {
                continue;
            }
            if ("*".equals(rule) || rule.equals(clientIp)) {
                return true;
            }
            if (rule.endsWith(".*") && clientIp != null && clientIp.startsWith(rule.substring(0, rule.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
