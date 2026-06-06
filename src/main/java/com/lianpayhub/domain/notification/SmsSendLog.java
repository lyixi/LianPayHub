package com.lianpayhub.domain.notification;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "sms_send_log", indexes = {
        @Index(name = "idx_sms_send_log_type_time", columnList = "channel_type,created_at"),
        @Index(name = "idx_sms_send_log_config_time", columnList = "config_id,created_at")
})
public class SmsSendLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_id")
    private Long configId;

    @Column(name = "channel_type", nullable = false, length = 32)
    private String channelType;

    @Column(name = "provider_code", length = 64)
    private String providerCode;

    @Column(name = "app_id", length = 64)
    private String appId;

    @Column(name = "mobile", nullable = false, length = 32)
    private String mobile;

    @Column(name = "template_code", length = 128)
    private String templateCode;

    @Lob
    @Column(name = "params_json")
    private String paramsJson;

    @Column(name = "message_id", length = 128)
    private String messageId;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "result_message", length = 512)
    private String resultMessage;

    protected SmsSendLog() {
    }

    public SmsSendLog(Long configId, String channelType, String providerCode, String appId, String mobile,
                      String templateCode, String paramsJson, String messageId, boolean success, String resultMessage) {
        this.configId = configId;
        this.channelType = channelType;
        this.providerCode = providerCode;
        this.appId = appId;
        this.mobile = mobile;
        this.templateCode = templateCode;
        this.paramsJson = paramsJson;
        this.messageId = messageId;
        this.success = success;
        this.resultMessage = resultMessage;
    }

    public Long getId() {
        return id;
    }

    public Long getConfigId() {
        return configId;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getAppId() {
        return appId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public String getMessageId() {
        return messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
