package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.PayChannel;

public interface PaymentProvider {
    String providerCode();
    PayChannel payChannel();
    PaymentCreateResult createPayment(PaymentCreateContext context);
    PaymentCallbackResult parseCallback(String rawPayload);

    default PaymentCallbackResult parseCallback(PaymentChannelContext context, String rawPayload) {
        return parseCallback(rawPayload);
    }

    default ChannelOrderResult queryOrder(PaymentChannelContext context, String orderNo, String tradeNo) {
        return ChannelOrderResult.unsupported();
    }

    default ChannelRefundResult refund(PaymentChannelContext context, String orderNo, String tradeNo,
                                       String refundNo, Integer amountCents, String reason) {
        return ChannelRefundResult.unsupported();
    }
}
