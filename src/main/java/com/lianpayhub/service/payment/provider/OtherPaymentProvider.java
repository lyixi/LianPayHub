package com.lianpayhub.service.payment.provider;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.service.payment.PaymentCallbackResult;
import com.lianpayhub.service.payment.PaymentCreateContext;
import com.lianpayhub.service.payment.PaymentCreateResult;
import com.lianpayhub.service.payment.PaymentProvider;
import com.lianpayhub.service.payment.SimplePaymentCallbackParser;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OtherPaymentProvider implements PaymentProvider {

    @Override
    public String providerCode() {
        return "other";
    }

    @Override
    public PayChannel payChannel() {
        return PayChannel.OTHER;
    }

    @Override
    public PaymentCreateResult createPayment(PaymentCreateContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", context.orderNo());
        params.put("amountCents", context.amountCents());
        params.put("subject", context.subject());
        params.put("provider", providerCode());
        return new PaymentCreateResult(context.orderNo(), providerCode(), params);
    }

    @Override
    public PaymentCallbackResult parseCallback(String rawPayload) {
        return SimplePaymentCallbackParser.parse(rawPayload);
    }
}
