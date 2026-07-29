package com.lianpayhub.domain.ai;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "app_ai_provider_setting", indexes = {
        @Index(name = "uk_app_ai_provider", columnList = "app_id,provider_code", unique = true),
        @Index(name = "idx_app_ai_provider_app", columnList = "app_id")
})
public class AppAiProviderSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "provider_code", nullable = false, length = 64)
    private String providerCode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "auto_provision_user_key", nullable = false)
    private boolean autoProvisionUserKey = false;

    @Column(name = "default_quota_units", nullable = false)
    private Long defaultQuotaUnits = 0L;

    @Column(name = "daily_limit_units", nullable = false)
    private Long dailyLimitUnits = 0L;

    @Column(name = "key_group_id", length = 128)
    private String keyGroupId;

    protected AppAiProviderSetting() {}

    public AppAiProviderSetting(String appId, String providerCode, boolean enabled, boolean autoProvisionUserKey,
                                Long defaultQuotaUnits, Long dailyLimitUnits, String keyGroupId) {
        this.appId = appId;
        this.providerCode = providerCode;
        this.enabled = enabled;
        this.autoProvisionUserKey = autoProvisionUserKey;
        this.defaultQuotaUnits = defaultQuotaUnits == null ? 0L : defaultQuotaUnits;
        this.dailyLimitUnits = dailyLimitUnits == null ? 0L : dailyLimitUnits;
        this.keyGroupId = keyGroupId;
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public String getProviderCode() { return providerCode; }
    public boolean isEnabled() { return enabled; }
    public boolean isAutoProvisionUserKey() { return autoProvisionUserKey; }
    public Long getDefaultQuotaUnits() { return defaultQuotaUnits; }
    public Long getDailyLimitUnits() { return dailyLimitUnits; }
    public String getKeyGroupId() { return keyGroupId; }

    public void update(boolean enabled, boolean autoProvisionUserKey, Long defaultQuotaUnits, Long dailyLimitUnits, String keyGroupId) {
        this.enabled = enabled;
        this.autoProvisionUserKey = autoProvisionUserKey;
        this.defaultQuotaUnits = defaultQuotaUnits == null ? 0L : defaultQuotaUnits;
        this.dailyLimitUnits = dailyLimitUnits == null ? 0L : dailyLimitUnits;
        this.keyGroupId = keyGroupId;
    }
}
