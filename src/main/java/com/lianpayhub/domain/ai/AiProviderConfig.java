package com.lianpayhub.domain.ai;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "ai_provider_config", indexes = {
        @Index(name = "uk_ai_provider_code", columnList = "provider_code", unique = true)
})
public class AiProviderConfig extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_code", nullable = false, length = 64, unique = true)
    private String providerCode;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "base_url", length = 512)
    private String baseUrl;

    @Column(name = "console_base_url", length = 512)
    private String consoleBaseUrl;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @Lob
    @Column(name = "credential_json")
    private String credentialJson;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    protected AiProviderConfig() {}

    public AiProviderConfig(String providerCode, String displayName, String baseUrl, String consoleBaseUrl,
                            String configJson, String credentialJson) {
        this.providerCode = providerCode;
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.consoleBaseUrl = consoleBaseUrl;
        this.configJson = configJson;
        this.credentialJson = credentialJson;
    }

    public Long getId() { return id; }
    public String getProviderCode() { return providerCode; }
    public String getDisplayName() { return displayName; }
    public String getBaseUrl() { return baseUrl; }
    public String getConsoleBaseUrl() { return consoleBaseUrl; }
    public String getConfigJson() { return configJson; }
    public String getCredentialJson() { return credentialJson; }
    public boolean isEnabled() { return enabled; }

    public void update(String displayName, String baseUrl, String consoleBaseUrl, String configJson, String credentialJson) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.consoleBaseUrl = consoleBaseUrl;
        this.configJson = configJson;
        if (credentialJson != null && !credentialJson.trim().isEmpty()) this.credentialJson = credentialJson;
    }

    public void changeEnabled(boolean enabled) { this.enabled = enabled; }
}
