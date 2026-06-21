package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PayMode;

public class CreateOrderCommand {
    private final String appId;
    private final Long userId;
    private final Long deviceId;
    private final Long packageId;
    private final PayChannel payChannel;
    private final PayMode payMode;
    private final String returnUrl;

    public CreateOrderCommand(String appId, Long userId, Long deviceId, Long packageId, PayChannel payChannel) {
        this(appId, userId, deviceId, packageId, payChannel, null, null);
    }

    public CreateOrderCommand(String appId, Long userId, Long deviceId, Long packageId, PayChannel payChannel,
                              PayMode payMode, String returnUrl) {
        this.appId = appId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.packageId = packageId;
        this.payChannel = payChannel;
        this.payMode = payMode;
        this.returnUrl = returnUrl;
    }

    public String appId() { return appId; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public Long packageId() { return packageId; }
    public PayChannel payChannel() { return payChannel; }
    public PayMode payMode() { return payMode; }
    public String returnUrl() { return returnUrl; }
}
