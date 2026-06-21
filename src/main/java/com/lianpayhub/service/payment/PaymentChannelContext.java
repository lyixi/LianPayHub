package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PaymentChannelConfig;

public class PaymentChannelContext {
    private final PaymentChannelConfig channelConfig;

    public PaymentChannelContext(PaymentChannelConfig channelConfig) {
        this.channelConfig = channelConfig;
    }

    public PaymentChannelConfig channelConfig() { return channelConfig; }
}
