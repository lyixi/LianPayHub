package com.lianpayhub.security;

import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppStatus;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.service.security.ApiSignatureService;
import com.lianpayhub.service.security.AppSecretService;
import com.lianpayhub.service.security.NonceCacheService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class ApiAppAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_APP_ID = "X-App-Id";
    public static final String HEADER_APP_SECRET = "X-App-Secret";
    public static final String HEADER_TIMESTAMP = "X-App-Timestamp";
    public static final String HEADER_NONCE = "X-App-Nonce";
    public static final String HEADER_SIGNATURE = "X-App-Signature";

    private final SecurityProperties securityProperties;
    private final AppInfoRepository appInfoRepository;
    private final AppSecretService appSecretService;
    private final ApiSignatureService apiSignatureService;
    private final NonceCacheService nonceCacheService;

    public ApiAppAuthenticationFilter(SecurityProperties securityProperties,
                                      AppInfoRepository appInfoRepository,
                                      AppSecretService appSecretService,
                                      ApiSignatureService apiSignatureService,
                                      NonceCacheService nonceCacheService) {
        this.securityProperties = securityProperties;
        this.appInfoRepository = appInfoRepository;
        this.appSecretService = appSecretService;
        this.apiSignatureService = apiSignatureService;
        this.nonceCacheService = nonceCacheService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !Boolean.TRUE.equals(securityProperties.getApiAuthEnabled())
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String appId = request.getHeader(HEADER_APP_ID);
        if (appId == null || appId.trim().isEmpty()) {
            writeError(response, 401, "缺少 APP 鉴权信息");
            return;
        }

        AppInfo appInfo = appInfoRepository.findByAppId(appId).orElse(null);
        if (appInfo == null || appInfo.getStatus() != AppStatus.ENABLED) {
            writeError(response, 401, "APP 不存在或已停用");
            return;
        }
        if (!authenticate(request, appInfo)) {
            writeError(response, 401, "APP 鉴权失败");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticate(HttpServletRequest request, AppInfo appInfo) {
        if ("signature".equalsIgnoreCase(securityProperties.getApiAuthMode())) {
            return authenticateBySignature(request, appInfo);
        }
        return authenticateBySecret(request, appInfo);
    }

    private boolean authenticateBySecret(HttpServletRequest request, AppInfo appInfo) {
        String appSecret = request.getHeader(HEADER_APP_SECRET);
        if (appSecret == null || appSecret.trim().isEmpty()) {
            return false;
        }
        return appSecretService.matches(appSecret, appInfo.getAppSecretHash());
    }

    private boolean authenticateBySignature(HttpServletRequest request, AppInfo appInfo) {
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);
        if (timestamp == null || nonce == null || signature == null) {
            return false;
        }
        if (!timestampValid(timestamp)) {
            return false;
        }
        String nonceKey = appInfo.getAppId() + ":" + nonce;
        if (!nonceCacheService.markIfAbsent(nonceKey, securityProperties.getApiSignatureTimeWindowSeconds())) {
            return false;
        }
        String expected = apiSignatureService.sign(
                appInfo.getAppId(),
                timestamp,
                nonce,
                request.getMethod(),
                request.getRequestURI(),
                appInfo.getAppSecretHash()
        );
        return apiSignatureService.matches(expected, signature);
    }

    private boolean timestampValid(String timestampText) {
        try {
            long timestamp = Long.parseLong(timestampText);
            long now = Instant.now().getEpochSecond();
            int window = securityProperties.getApiSignatureTimeWindowSeconds();
            return Math.abs(now - timestamp) <= window;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
