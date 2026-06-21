package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PayMode;
import com.lianpayhub.domain.payment.PaymentChannelConfig;

public class PaymentCreateContext {
    private final String orderNo;
    private final Integer amountCents;
    private final String subject;
    private final PayMode payMode;
    private final String returnUrl;
    private final PaymentChannelConfig channelConfig;

    public PaymentCreateContext(String orderNo, Integer amountCents, String subject) {
        this(orderNo, amountCents, subject, null, null, null);
    }

    public PaymentCreateContext(String orderNo, Integer amountCents, String subject, PayMode payMode,
                                String returnUrl, PaymentChannelConfig channelConfig) {
        this.orderNo = orderNo;
        this.amountCents = amountCents;
        this.subject = subject;
        this.payMode = payMode;
        this.returnUrl = returnUrl;
        this.channelConfig = channelConfig;
    }

    public String orderNo() { return orderNo; }
    public Integer amountCents() { return amountCents; }
    public String subject() { return subject; }
    public PayMode payMode() { return payMode; }
    public String returnUrl() { return returnUrl; }
    public PaymentChannelConfig channelConfig() { return channelConfig; }
}
