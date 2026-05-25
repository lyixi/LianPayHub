package com.lianpayhub.service.report;

public class AdminOverviewResult {

    private final long appCount;
    private final long userCount;
    private final long deviceCount;
    private final long memberCount;
    private final long orderCount;
    private final long paidOrderCount;
    private final long paidAmountCents;
    private final long launchCount;
    private final long adapterReportCount;
    private final long appLoginLogCount;
    private final long paymentEventLogCount;

    public AdminOverviewResult(long appCount, long userCount, long deviceCount, long memberCount,
                               long orderCount, long paidOrderCount, long paidAmountCents,
                               long launchCount, long adapterReportCount, long appLoginLogCount,
                               long paymentEventLogCount) {
        this.appCount = appCount;
        this.userCount = userCount;
        this.deviceCount = deviceCount;
        this.memberCount = memberCount;
        this.orderCount = orderCount;
        this.paidOrderCount = paidOrderCount;
        this.paidAmountCents = paidAmountCents;
        this.launchCount = launchCount;
        this.adapterReportCount = adapterReportCount;
        this.appLoginLogCount = appLoginLogCount;
        this.paymentEventLogCount = paymentEventLogCount;
    }

    public long getAppCount() { return appCount; }
    public long getUserCount() { return userCount; }
    public long getDeviceCount() { return deviceCount; }
    public long getMemberCount() { return memberCount; }
    public long getOrderCount() { return orderCount; }
    public long getPaidOrderCount() { return paidOrderCount; }
    public long getPaidAmountCents() { return paidAmountCents; }
    public long getLaunchCount() { return launchCount; }
    public long getAdapterReportCount() { return adapterReportCount; }
    public long getAppLoginLogCount() { return appLoginLogCount; }
    public long getPaymentEventLogCount() { return paymentEventLogCount; }
}
