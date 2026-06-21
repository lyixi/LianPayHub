package com.lianpayhub.web.api;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PayMode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotBlank
    private String appId;
    private Long userId;
    private Long deviceId;
    @NotNull
    private Long packageId;
    private PayChannel payChannel;
    private PayMode payMode;
    private String returnUrl;

    public String appId() { return appId; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public Long packageId() { return packageId; }
    public PayChannel payChannel() { return payChannel; }
    public PayMode payMode() { return payMode; }
    public String returnUrl() { return returnUrl; }
}
