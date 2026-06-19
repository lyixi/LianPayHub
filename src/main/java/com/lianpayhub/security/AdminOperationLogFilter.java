package com.lianpayhub.security;

import com.lianpayhub.domain.log.AdminOperationLog;
import com.lianpayhub.domain.log.LogResultStatus;
import com.lianpayhub.repository.AdminOperationLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AdminOperationLogFilter extends OncePerRequestFilter {

    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(\"(?:password|oldPassword|newPassword|appSecret|token|credentialJson|privateKey|apiKey|merchantKey|certPassword|secretId|secretKey|accessKeyId|accessKeySecret|smtpPassword|authorizationCode)\"\\s*:\\s*\")((?:\\\\.|[^\"\\\\])*)(\")",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CONFIRM_REASON_FIELD = Pattern.compile(
            "\"(?:confirmReason|reason)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
            Pattern.CASE_INSENSITIVE
    );

    private final AdminOperationLogRepository logRepository;

    public AdminOperationLogFilter(AdminOperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/admin/") || uri.startsWith("/admin/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
        Throwable failure = null;
        try {
            filterChain.doFilter(wrapped, response);
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            saveLog(wrapped, response, failure);
        }
    }

    private void saveLog(CachedBodyHttpServletRequest request, HttpServletResponse response, Throwable failure) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AdminPrincipal principal = authentication == null ? null : asAdminPrincipal(authentication.getPrincipal());
        int status = response.getStatus();
        LogResultStatus resultStatus = failure == null && status < 400 ? LogResultStatus.SUCCESS : LogResultStatus.FAILED;
        String errorMessage = failure == null ? null : failure.getMessage();
        String body = request.getCachedBodyAsString();
        logRepository.save(new AdminOperationLog(
                principal == null ? null : principal.getAdminId(),
                principal == null ? null : principal.getUsername(),
                guessOperationType(request.getMethod(), request.getRequestURI()),
                guessTargetType(request.getRequestURI()),
                guessTargetId(request.getRequestURI()),
                request.getMethod(),
                request.getRequestURI(),
                clientIp(request),
                request.getHeader("User-Agent"),
                trimBody(maskSensitiveBody(body)),
                trimBody(extractConfirmReason(body)),
                resultStatus,
                errorMessage
        ));
    }

    private AdminPrincipal asAdminPrincipal(Object principal) {
        return principal instanceof AdminPrincipal ? (AdminPrincipal) principal : null;
    }

    private String trimBody(String body) {
        if (body == null || body.length() <= 2000) {
            return body;
        }
        return body.substring(0, 2000);
    }

    private String maskSensitiveBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        return SENSITIVE_JSON_FIELD.matcher(body).replaceAll("$1******$3");
    }

    private String extractConfirmReason(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        Matcher matcher = CONFIRM_REASON_FIELD.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.trim().isEmpty() ? request.getRemoteAddr() : realIp.trim();
    }

    private String guessOperationType(String method, String uri) {
        if ("GET".equalsIgnoreCase(method)) {
            return "QUERY";
        }
        if (uri.endsWith("/reset-secret")) {
            return "RESET_SECRET";
        }
        if (uri.endsWith("/device-code")) {
            return "CHANGE_DEVICE_CODE";
        }
        if (uri.endsWith("/mark-paid")) {
            return "MARK_ORDER_PAID";
        }
        if (uri.endsWith("/close")) {
            return "CLOSE_ORDER";
        }
        if (uri.endsWith("/mark-success")) {
            return "MARK_REFUND_SUCCESS";
        }
        if (uri.endsWith("/mark-failed")) {
            return "MARK_REFUND_FAILED";
        }
        if (uri.endsWith("/cancel")) {
            return "CANCEL_MEMBER";
        }
        if (uri.endsWith("/unbind")) {
            return "UNBIND_DEVICE";
        }
        if (uri.endsWith("/status")) {
            return "CHANGE_STATUS";
        }
        if ("POST".equalsIgnoreCase(method)) {
            return "CREATE";
        }
        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            return "UPDATE";
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return "DELETE";
        }
        return "OTHER";
    }

    private String guessTargetType(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private String guessTargetId(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 3 ? parts[3] : null;
    }
}
