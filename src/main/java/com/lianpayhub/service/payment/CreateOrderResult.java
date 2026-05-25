package com.lianpayhub.service.payment;

import java.util.Map;

public class CreateOrderResult {
    private final String orderNo;
    private final Integer amountCents;
    private final Map<String, Object> paymentParams;

    public CreateOrderResult(String orderNo, Integer amountCents, Map<String, Object> paymentParams) {
        this.orderNo = orderNo;
        this.amountCents = amountCents;
        this.paymentParams = paymentParams;
    }

    public String getOrderNo() { return orderNo; }
    public Integer getAmountCents() { return amountCents; }
    public Map<String, Object> getPaymentParams() { return paymentParams; }
}
