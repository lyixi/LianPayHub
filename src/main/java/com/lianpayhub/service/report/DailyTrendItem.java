package com.lianpayhub.service.report;

public class DailyTrendItem {
    private final String date;
    private final long orderCount;
    private final long paidOrderCount;
    private final long paidAmountCents;
    private final long loginCount;
    private final long launchCount;

    public DailyTrendItem(String date, long orderCount, long paidOrderCount,
                          long paidAmountCents, long loginCount, long launchCount) {
        this.date = date;
        this.orderCount = orderCount;
        this.paidOrderCount = paidOrderCount;
        this.paidAmountCents = paidAmountCents;
        this.loginCount = loginCount;
        this.launchCount = launchCount;
    }

    public String getDate() { return date; }
    public long getOrderCount() { return orderCount; }
    public long getPaidOrderCount() { return paidOrderCount; }
    public long getPaidAmountCents() { return paidAmountCents; }
    public long getLoginCount() { return loginCount; }
    public long getLaunchCount() { return launchCount; }
}
