package com.lianpayhub.service.payment;

public class PaymentNotifyResult {
    private final String orderNo;
    private final String tradeNo;
    private final boolean processed;
    private final String message;

    public PaymentNotifyResult(String orderNo, String tradeNo, boolean processed, String message) {
        this.orderNo = orderNo;
        this.tradeNo = tradeNo;
        this.processed = processed;
        this.message = message;
    }

    public String orderNo() { return orderNo; }
    public String tradeNo() { return tradeNo; }
    public boolean processed() { return processed; }
    public String message() { return message; }
}
