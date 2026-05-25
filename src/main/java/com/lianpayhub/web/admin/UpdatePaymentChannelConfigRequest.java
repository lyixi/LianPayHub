package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class UpdatePaymentChannelConfigRequest {
    @NotBlank
    private String providerCode;
    private String merchantId;
    private String channelAppId;
    private String notifyUrl;
    private String configJson;
    private String credentialJson;

    public String providerCode() { return providerCode; }
    public String merchantId() { return merchantId; }
    public String channelAppId() { return channelAppId; }
    public String notifyUrl() { return notifyUrl; }
    public String configJson() { return configJson; }
    public String credentialJson() { return credentialJson; }
}
