package com.lianpayhub.service.payment;

public class RefundCommand {
    private final Long orderId;
    private final Integer amountCents;
    private final String reason;
    private final String operatorId;

    public RefundCommand(Long orderId, Integer amountCents, String reason, String operatorId) {
        this.orderId = orderId;
        this.amountCents = amountCents;
        this.reason = reason;
        this.operatorId = operatorId;
    }

    public Long orderId() { return orderId; }
    public Integer amountCents() { return amountCents; }
    public String reason() { return reason; }
    public String operatorId() { return operatorId; }
}
