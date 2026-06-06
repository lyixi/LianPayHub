package com.lianpayhub.web.admin;

import com.lianpayhub.domain.notification.NotificationChannelType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateNotificationChannelConfigRequest {
    @NotNull
    private NotificationChannelType channelType;
    @NotBlank
    private String providerCode;
    @NotBlank
    private String displayName;
    private String senderName;
    private String senderAddress;
    private String endpoint;
    private String templateCode;
    private String accessKeyId;
    private String accessKeySecret;
    private String secretId;
    private String secretKey;
    private String sdkAppId;
    private String region;
    private String configJson;
    private String credentialJson;

    public NotificationChannelType channelType() { return channelType; }
    public String providerCode() { return providerCode; }
    public String displayName() { return displayName; }
    public String senderName() { return senderName; }
    public String senderAddress() { return senderAddress; }
    public String endpoint() { return endpoint; }
    public String templateCode() { return templateCode; }
    public String accessKeyId() { return accessKeyId; }
    public String accessKeySecret() { return accessKeySecret; }
    public String secretId() { return secretId; }
    public String secretKey() { return secretKey; }
    public String sdkAppId() { return sdkAppId; }
    public String region() { return region; }
    public String configJson() { return configJson; }
    public String credentialJson() { return credentialJson; }
}
