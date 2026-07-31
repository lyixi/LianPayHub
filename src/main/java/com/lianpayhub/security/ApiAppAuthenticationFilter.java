package com.lianpayhub.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.config.SecurityProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppStatus;
import com.lianpayhub.domain.app.AppType;
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
    private final ObjectMapper objectMapper;

    public ApiAppAuthenticationFilter(SecurityProperties securityProperties,
                                      AppInfoRepository appInfoRepository,
                                      AppSecretService appSecretService,
                                      ApiSignatureService apiSignatureService,
                                      NonceCacheService nonceCacheService,
                                      ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.appInfoRepository = appInfoRepository;
        this.appSecretService = appSecretService;
        this.apiSignatureService = apiSignatureService;
        this.nonceCacheService = nonceCacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !Boolean.TRUE.equals(securityProperties.getApiAuthEnabled())
                || !request.getRequestURI().startsWith("/api/")
                || isRefreshTokenAuthRequest(request)
                || request.getRequestURI().startsWith("/api/payment/notify/");
    }

    private boolean isRefreshTokenAuthRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/api/auth/refresh".equals(uri) || "/api/auth/logout".equals(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest requestToUse = request;
        String appId = resolveAppId(request);
        if (isBlank(appId) && canHaveJsonBody(request)) {
            CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
            requestToUse = wrapped;
            appId = resolveAppIdFromBody(wrapped);
        }
        if (appId == null || appId.trim().isEmpty()) {
            writeError(response, 401, "缺少 APP 鉴权信息");
            return;
        }

        AppInfo appInfo = appInfoRepository.findByAppId(appId).orElse(null);
        if (appInfo == null || appInfo.getStatus() != AppStatus.ENABLED) {
            writeError(response, 401, "APP 不存在或已停用");
            return;
        }
        if (isDeviceOnlyPublicRequest(requestToUse, appInfo)) {
            filterChain.doFilter(requestToUse, response);
            return;
        }
        if (!authenticate(requestToUse, appInfo)) {
            writeError(response, 401, "APP 鉴权失败");
            return;
        }

        filterChain.doFilter(requestToUse, response);
    }

    private String resolveAppId(HttpServletRequest request) {
        String headerAppId = request.getHeader(HEADER_APP_ID);
        if (!isBlank(headerAppId)) {
            return headerAppId.trim();
        }
        String parameterAppId = request.getParameter("appId");
        return isBlank(parameterAppId) ? null : parameterAppId.trim();
    }

    private String resolveAppIdFromBody(CachedBodyHttpServletRequest request) {
        String body = request.getCachedBodyAsString();
        if (isBlank(body)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode appIdNode = root.path("appId");
            return appIdNode.isMissingNode() || isBlank(appIdNode.asText()) ? null : appIdNode.asText().trim();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean canHaveJsonBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private boolean isDeviceOnlyPublicRequest(HttpServletRequest request, AppInfo appInfo) {
        if (appInfo.getAppType() != AppType.DEVICE_ONLY) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/api/device/")
                || "/api/member/status".equals(uri)
                || "/api/payment/create-order".equals(uri);
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
        String expected = apiSignatureService.sign(
                appInfo.getAppId(),
                timestamp,
                nonce,
                request.getMethod(),
                request.getRequestURI(),
                appInfo.getAppSecretHash()
        );
        if (!apiSignatureService.matches(expected, signature)) {
            return false;
        }
        String nonceKey = appInfo.getAppId() + ":" + nonce;
        return nonceCacheService.markIfAbsent(nonceKey, securityProperties.getApiSignatureTimeWindowSeconds());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
