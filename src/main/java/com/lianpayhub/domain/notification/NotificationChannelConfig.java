package com.lianpayhub.domain.notification;

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
@Table(name = "notification_channel_config", indexes = {
        @Index(name = "uk_notification_channel_provider", columnList = "channel_type,provider_code", unique = true),
        @Index(name = "idx_notification_channel_status", columnList = "channel_type,status")
})
public class NotificationChannelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 32)
    private NotificationChannelType channelType;

    @Column(name = "provider_code", nullable = false, length = 64)
    private String providerCode;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "sender_name", length = 128)
    private String senderName;

    @Column(name = "sender_address", length = 256)
    private String senderAddress;

    @Column(name = "endpoint", length = 512)
    private String endpoint;

    @Column(name = "template_code", length = 128)
    private String templateCode;

    @Column(name = "access_key_id", length = 256)
    private String accessKeyId;

    @JsonIgnore
    @Column(name = "access_key_secret", length = 512)
    private String accessKeySecret;

    @Column(name = "secret_id", length = 256)
    private String secretId;

    @JsonIgnore
    @Column(name = "secret_key", length = 512)
    private String secretKey;

    @Column(name = "sdk_app_id", length = 128)
    private String sdkAppId;

    @Column(name = "region", length = 128)
    private String region;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @JsonIgnore
    @Lob
    @Column(name = "credential_json")
    private String credentialJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannelStatus status = NotificationChannelStatus.ENABLED;

    protected NotificationChannelConfig() {
    }

    public NotificationChannelConfig(NotificationChannelType channelType, String providerCode, String displayName,
                                     String senderName, String senderAddress, String endpoint,
                                     String templateCode, String accessKeyId, String accessKeySecret,
                                     String secretId, String secretKey, String sdkAppId, String region,
                                     String configJson, String credentialJson) {
        this.channelType = channelType;
        this.providerCode = providerCode;
        this.displayName = displayName;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.endpoint = endpoint;
        this.templateCode = templateCode;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.sdkAppId = sdkAppId;
        this.region = region;
        this.configJson = configJson;
        this.credentialJson = credentialJson;
    }

    public void update(String providerCode, String displayName, String senderName, String senderAddress,
                       String endpoint, String templateCode, String accessKeyId, String accessKeySecret,
                       String secretId, String secretKey, String sdkAppId, String region,
                       String configJson, String credentialJson) {
        this.providerCode = providerCode;
        this.displayName = displayName;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.endpoint = endpoint;
        this.templateCode = templateCode;
        this.accessKeyId = accessKeyId;
        if (accessKeySecret != null && !accessKeySecret.trim().isEmpty()) {
            this.accessKeySecret = accessKeySecret;
        }
        this.secretId = secretId;
        if (secretKey != null && !secretKey.trim().isEmpty()) {
            this.secretKey = secretKey;
        }
        this.sdkAppId = sdkAppId;
        this.region = region;
        this.configJson = configJson;
        if (credentialJson != null && !credentialJson.trim().isEmpty()) {
            this.credentialJson = credentialJson;
        }
    }

    public void changeStatus(NotificationChannelStatus status) {
        this.status = status;
    }

    public boolean isEnabled() {
        return status == NotificationChannelStatus.ENABLED;
    }

    public Long getId() {
        return id;
    }

    public NotificationChannelType getChannelType() {
        return channelType;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getSdkAppId() {
        return sdkAppId;
    }

    public String getRegion() {
        return region;
    }

    public String getConfigJson() {
        return configJson;
    }

    public String getCredentialJson() {
        return credentialJson;
    }

    public boolean isCredentialConfigured() {
        return accessKeySecret != null && !accessKeySecret.trim().isEmpty()
                || secretKey != null && !secretKey.trim().isEmpty()
                || credentialJson != null && !credentialJson.trim().isEmpty();
    }

    public NotificationChannelStatus getStatus() {
        return status;
    }
}
