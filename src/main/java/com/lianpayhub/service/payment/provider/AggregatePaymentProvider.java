package com.lianpayhub.service.payment.provider;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.service.payment.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AggregatePaymentProvider implements PaymentProvider {

    @Override
    public String providerCode() {
        return "aggregate";
    }

    @Override
    public PayChannel payChannel() {
        return PayChannel.AGGREGATE;
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
        // 聚合支付服务商未确定前，先支持通用字段解析；后续在此完成服务商验签。
        return SimplePaymentCallbackParser.parse(rawPayload);
    }
}
