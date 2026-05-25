package com.lianpayhub.service.payment.provider;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.service.payment.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AlipayPaymentProvider implements PaymentProvider {

    @Override
    public String providerCode() {
        return "alipay";
    }

    @Override
    public PayChannel payChannel() {
        return PayChannel.ALIPAY;
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
        // 支付宝 SDK 配置完成后，在这里替换为官方验签和字段解析。
        return SimplePaymentCallbackParser.parse(rawPayload);
    }
}
