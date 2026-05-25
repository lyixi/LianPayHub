package com.lianpayhub.service.payment;

import java.util.Map;

public class PaymentCreateResult {
    private final String orderNo;
    private final String providerCode;
    private final Map<String, Object> paymentParams;

    public PaymentCreateResult(String orderNo, String providerCode, Map<String, Object> paymentParams) {
        this.orderNo = orderNo;
        this.providerCode = providerCode;
        this.paymentParams = paymentParams;
    }

    public String orderNo() { return orderNo; }
    public String providerCode() { return providerCode; }
    public Map<String, Object> paymentParams() { return paymentParams; }
}
