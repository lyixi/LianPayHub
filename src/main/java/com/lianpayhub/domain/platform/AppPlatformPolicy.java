package com.lianpayhub.domain.platform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lianpayhub.domain.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "app_platform_policy", indexes = {
        @Index(name = "uk_app_platform_policy_app_category", columnList = "app_id,category", unique = true),
        @Index(name = "idx_app_platform_policy_category", columnList = "category,enabled")
})
public class AppPlatformPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private PlatformConfigCategory category;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "provider_code", length = 64)
    private String providerCode;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @JsonIgnore
    @Lob
    @Column(name = "credential_json")
    private String credentialJson;

    @Lob
    @Column(name = "policy_json")
    private String policyJson;

    protected AppPlatformPolicy() {
    }

    public AppPlatformPolicy(String appId, PlatformConfigCategory category, boolean enabled,
                             String providerCode, String configJson, String credentialJson, String policyJson) {
        this.appId = appId;
        this.category = category;
        update(enabled, providerCode, configJson, credentialJson, policyJson);
    }

    public void update(boolean enabled, String providerCode, String configJson, String credentialJson,
                       String policyJson) {
        this.enabled = enabled;
        this.providerCode = normalize(providerCode);
        this.configJson = normalize(configJson);
        if (credentialJson != null && !credentialJson.trim().isEmpty()) {
            this.credentialJson = credentialJson.trim();
        }
        this.policyJson = normalize(policyJson);
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public PlatformConfigCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getConfigJson() {
        return configJson;
    }

    public String getCredentialJson() {
        return credentialJson;
    }

    public String getPolicyJson() {
        return policyJson;
    }

    public boolean isCredentialConfigured() {
        return credentialJson != null && !credentialJson.trim().isEmpty();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
