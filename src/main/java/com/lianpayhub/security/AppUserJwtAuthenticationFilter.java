package com.lianpayhub.security;

import com.lianpayhub.service.security.JwtService;
import com.lianpayhub.repository.UserInfoRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.domain.device.DeviceBindStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AppUserJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AppUserJwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserInfoRepository userInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    public AppUserJwtAuthenticationFilter(JwtService jwtService, UserInfoRepository userInfoRepository,
                                          DeviceInfoRepository deviceInfoRepository) {
        this.jwtService = jwtService;
        this.userInfoRepository = userInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token);
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

    private void authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            if (!"USER".equals(claims.get("type", String.class))) {
                return;
            }
            Long userId = Long.valueOf(claims.getSubject());
            String appId = claims.get("appId", String.class);
            String mobile = claims.get("mobile", String.class);
            String deviceCode = claims.get("deviceCode", String.class);
            Number tokenVersionNumber = claims.get("tokenVersion", Number.class);
            Long tokenVersion = tokenVersionNumber == null ? null : tokenVersionNumber.longValue();
            if (!isTokenVersionValid(userId, tokenVersion)) {
                log.info("APP 用户 JWT 拒绝：tokenVersion 不匹配 userId={} appId={}", userId, appId);
                return;
            }
            if (!isDeviceAllowed(appId, deviceCode)) {
                log.info("APP 用户 JWT 拒绝：设备已拉黑 appId={} deviceCode={}", appId, deviceCode);
                return;
            }
            AppUserPrincipal principal = new AppUserPrincipal(userId, appId, mobile, deviceCode);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException ex) {
            log.info("APP 用户 JWT 拒绝：access token 已过期 subject={}", ex.getClaims() == null ? null : ex.getClaims().getSubject());
            SecurityContextHolder.clearContext();
        } catch (RuntimeException ignored) {
            log.info("APP 用户 JWT 拒绝：token 无效");
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isTokenVersionValid(Long userId, Long tokenVersion) {
        return userInfoRepository.findById(userId)
                .map(user -> tokenVersion == null || tokenVersion.equals(user.getTokenVersion()))
                .orElse(false);
    }

    private boolean isDeviceAllowed(String appId, String deviceCode) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(deviceCode)) {
            return true;
        }
        return deviceInfoRepository.findByAppIdAndDeviceCode(appId, deviceCode.trim())
                .map(device -> device.getBindStatus() != DeviceBindStatus.BLACKLISTED)
                .orElse(true);
    }
}
