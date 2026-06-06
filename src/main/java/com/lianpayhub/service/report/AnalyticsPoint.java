package com.lianpayhub.service.report;

public class AnalyticsPoint {
    private final String period;
    private final long value;
    private final long amountCents;

    public AnalyticsPoint(String period, long value, long amountCents) {
        this.period = period;
        this.value = value;
        this.amountCents = amountCents;
    }

    public String getPeriod() { return period; }
    public long getValue() { return value; }
    public long getAmountCents() { return amountCents; }
}
