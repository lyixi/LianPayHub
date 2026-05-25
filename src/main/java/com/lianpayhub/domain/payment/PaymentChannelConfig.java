package com.lianpayhub.domain.payment;

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
@Table(name = "payment_channel_config", indexes = {
        @Index(name = "uk_payment_channel_config_app_channel", columnList = "app_id,pay_channel", unique = true),
        @Index(name = "idx_payment_channel_config_status", columnList = "status")
})
public class PaymentChannelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", nullable = false, length = 32)
    private PayChannel payChannel;

    @Column(name = "provider_code", nullable = false, length = 64)
    private String providerCode;

    @Column(name = "merchant_id", length = 128)
    private String merchantId;

    @Column(name = "channel_app_id", length = 128)
    private String channelAppId;

    @Column(name = "notify_url", length = 512)
    private String notifyUrl;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @JsonIgnore
    @Lob
    @Column(name = "credential_json")
    private String credentialJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentChannelConfigStatus status = PaymentChannelConfigStatus.ENABLED;

    protected PaymentChannelConfig() {
    }

    public PaymentChannelConfig(String appId, PayChannel payChannel, String providerCode, String merchantId,
                                String channelAppId, String notifyUrl, String configJson, String credentialJson) {
        this.appId = appId;
        this.payChannel = payChannel;
        this.providerCode = providerCode;
        this.merchantId = merchantId;
        this.channelAppId = channelAppId;
        this.notifyUrl = notifyUrl;
        this.configJson = configJson;
        this.credentialJson = credentialJson;
    }

    public void update(String providerCode, String merchantId, String channelAppId, String notifyUrl,
                       String configJson, String credentialJson) {
        this.providerCode = providerCode;
        this.merchantId = merchantId;
        this.channelAppId = channelAppId;
        this.notifyUrl = notifyUrl;
        this.configJson = configJson;
        if (credentialJson != null && !credentialJson.trim().isEmpty()) {
            this.credentialJson = credentialJson;
        }
    }

    public void changeStatus(PaymentChannelConfigStatus status) {
        this.status = status;
    }

    public boolean isEnabled() {
        return status == PaymentChannelConfigStatus.ENABLED;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public PayChannel getPayChannel() {
        return payChannel;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getChannelAppId() {
        return channelAppId;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getConfigJson() {
        return configJson;
    }

    public String getCredentialJson() {
        return credentialJson;
    }

    public boolean isCredentialConfigured() {
        return credentialJson != null && !credentialJson.trim().isEmpty();
    }

    public PaymentChannelConfigStatus getStatus() {
        return status;
    }
}
