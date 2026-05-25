package com.lianpayhub.service.payment;

public class PaymentCreateContext {
    private final String orderNo;
    private final Integer amountCents;
    private final String subject;

    public PaymentCreateContext(String orderNo, Integer amountCents, String subject) {
        this.orderNo = orderNo;
        this.amountCents = amountCents;
        this.subject = subject;
    }

    public String orderNo() { return orderNo; }
    public Integer amountCents() { return amountCents; }
    public String subject() { return subject; }
}
