package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PayChannel;

public interface PaymentProvider {
    String providerCode();
    PayChannel payChannel();
    PaymentCreateResult createPayment(PaymentCreateContext context);
    PaymentCallbackResult parseCallback(String rawPayload);
}
