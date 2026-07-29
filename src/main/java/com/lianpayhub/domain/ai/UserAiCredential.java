package com.lianpayhub.domain.ai;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "user_ai_credential", indexes = {
        @Index(name = "uk_user_ai_credential", columnList = "user_id,app_id,provider_code", unique = true),
        @Index(name = "idx_user_ai_provider", columnList = "provider_code")
})
public class UserAiCredential extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "provider_code", length = 64)
    private String providerCode;

    @Column(name = "api_key", length = 512)
    private String apiKey;

    @Column(name = "quota_units", nullable = false)
    private Long quotaUnits = 0L;

    protected UserAiCredential() {}

    public UserAiCredential(Long userId, String appId, String providerCode, String apiKey, Long quotaUnits) {
        this.userId = userId;
        this.appId = appId;
        this.providerCode = providerCode;
        this.apiKey = apiKey;
        this.quotaUnits = quotaUnits == null ? 0L : quotaUnits;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public String getProviderCode() { return providerCode; }
    public String getApiKey() { return apiKey; }
    public Long getQuotaUnits() { return quotaUnits; }

    public void update(String providerCode, String apiKey, Long quotaUnits) {
        this.providerCode = providerCode;
        if (apiKey != null && !apiKey.trim().isEmpty()) this.apiKey = apiKey;
        this.quotaUnits = quotaUnits == null ? 0L : quotaUnits;
    }
}
