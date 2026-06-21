package com.lianpayhub.web.api;

import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.domain.payment.PaymentOrder;

public class PublicPaymentOrderResult {
    private final String orderNo;
    private final Integer amountCents;
    private final PayChannel payChannel;
    private final PayStatus payStatus;
    private final String tradeNo;

    public PublicPaymentOrderResult(PaymentOrder order) {
        this.orderNo = order.getOrderNo();
        this.amountCents = order.getAmountCents();
        this.payChannel = order.getPayChannel();
        this.payStatus = order.getPayStatus();
        this.tradeNo = order.getTradeNo();
    }

    public String getOrderNo() { return orderNo; }
    public Integer getAmountCents() { return amountCents; }
    public PayChannel getPayChannel() { return payChannel; }
    public PayStatus getPayStatus() { return payStatus; }
    public String getTradeNo() { return tradeNo; }
}
