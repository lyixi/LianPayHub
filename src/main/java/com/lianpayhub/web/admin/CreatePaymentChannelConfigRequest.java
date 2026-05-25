package com.lianpayhub.web.admin;

import com.lianpayhub.domain.payment.PayChannel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreatePaymentChannelConfigRequest {
    @NotBlank
    private String appId;
    @NotNull
    private PayChannel payChannel;
    @NotBlank
    private String providerCode;
    private String merchantId;
    private String channelAppId;
    private String notifyUrl;
    private String configJson;
    private String credentialJson;

    public String appId() { return appId; }
    public PayChannel payChannel() { return payChannel; }
    public String providerCode() { return providerCode; }
    public String merchantId() { return merchantId; }
    public String channelAppId() { return channelAppId; }
    public String notifyUrl() { return notifyUrl; }
    public String configJson() { return configJson; }
    public String credentialJson() { return credentialJson; }
}
