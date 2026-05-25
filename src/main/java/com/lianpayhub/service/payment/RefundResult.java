package com.lianpayhub.service.payment;

import com.lianpayhub.domain.payment.RefundStatus;

public class RefundResult {
    private final Long id;
    private final String refundNo;
    private final Integer amountCents;
    private final RefundStatus status;

    public RefundResult(Long id, String refundNo, Integer amountCents, RefundStatus status) {
        this.id = id;
        this.refundNo = refundNo;
        this.amountCents = amountCents;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getRefundNo() { return refundNo; }
    public Integer getAmountCents() { return amountCents; }
    public RefundStatus getStatus() { return status; }
}
