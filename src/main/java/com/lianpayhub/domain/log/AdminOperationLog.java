package com.lianpayhub.domain.log;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_operation_log", indexes = {
        @Index(name = "idx_admin_operation_admin", columnList = "admin_id"),
        @Index(name = "idx_admin_operation_time", columnList = "created_at")
})
public class AdminOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(length = 64)
    private String username;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "target_id", length = 128)
    private String targetId;

    @Column(name = "request_method", length = 16)
    private String requestMethod;

    @Column(name = "request_uri", length = 512)
    private String requestUri;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Lob
    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "confirm_reason", length = 512)
    private String confirmReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 32)
    private LogResultStatus resultStatus;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AdminOperationLog() {
    }

    public AdminOperationLog(Long adminId, String username, String operationType, String targetType, String targetId,
                             String requestMethod, String requestUri, String ipAddress, String userAgent,
                             String requestBody, LogResultStatus resultStatus, String errorMessage) {
        this(adminId, username, operationType, targetType, targetId, requestMethod, requestUri, ipAddress,
                userAgent, requestBody, null, resultStatus, errorMessage);
    }

    public AdminOperationLog(Long adminId, String username, String operationType, String targetType, String targetId,
                             String requestMethod, String requestUri, String ipAddress, String userAgent,
                             String requestBody, String confirmReason, LogResultStatus resultStatus, String errorMessage) {
        this.adminId = adminId;
        this.username = username;
        this.operationType = operationType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.requestMethod = requestMethod;
        this.requestUri = requestUri;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestBody = requestBody;
        this.confirmReason = confirmReason;
        this.resultStatus = resultStatus;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public String getUsername() { return username; }
    public String getOperationType() { return operationType; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getRequestMethod() { return requestMethod; }
    public String getRequestUri() { return requestUri; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getRequestBody() { return requestBody; }
    public String getConfirmReason() { return confirmReason; }
    public LogResultStatus getResultStatus() { return resultStatus; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
