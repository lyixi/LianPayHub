package com.lianpayhub.service.report;

public class PaymentSummaryItem {

    private final String dimension;
    private final long orderCount;
    private final long paidOrderCount;
    private final long paidAmountCents;

    public PaymentSummaryItem(String dimension, long orderCount, long paidOrderCount, long paidAmountCents) {
        this.dimension = dimension;
        this.orderCount = orderCount;
        this.paidOrderCount = paidOrderCount;
        this.paidAmountCents = paidAmountCents;
    }

    public String getDimension() { return dimension; }
    public long getOrderCount() { return orderCount; }
    public long getPaidOrderCount() { return paidOrderCount; }
    public long getPaidAmountCents() { return paidAmountCents; }
}
