package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PayChannel;

public class CreateOrderCommand {
    private final String appId;
    private final Long userId;
    private final Long deviceId;
    private final Long packageId;
    private final PayChannel payChannel;

    public CreateOrderCommand(String appId, Long userId, Long deviceId, Long packageId, PayChannel payChannel) {
        this.appId = appId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.packageId = packageId;
        this.payChannel = payChannel;
    }

    public String appId() { return appId; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public Long packageId() { return packageId; }
    public PayChannel payChannel() { return payChannel; }
}
