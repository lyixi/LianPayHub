package com.lianpayhub.web.admin;

public class AdminStatsResult {
    private final long packageCount;
    private final long bindingCount;
    private final long deviceCount;
    private final long memberCount;
    private final long orderCount;
    private final long paidOrderCount;
    private final long pendingOrderCount;
    private final long launchCount;
    private final long loginCount;
    private final long paidAmountCents;

    public AdminStatsResult(long packageCount, long bindingCount, long deviceCount, long memberCount,
                            long orderCount, long paidOrderCount, long pendingOrderCount,
                            long launchCount, long loginCount, long paidAmountCents) {
        this.packageCount = packageCount;
        this.bindingCount = bindingCount;
        this.deviceCount = deviceCount;
        this.memberCount = memberCount;
        this.orderCount = orderCount;
        this.paidOrderCount = paidOrderCount;
        this.pendingOrderCount = pendingOrderCount;
        this.launchCount = launchCount;
        this.loginCount = loginCount;
        this.paidAmountCents = paidAmountCents;
    }

    public long getPackageCount() { return packageCount; }
    public long getBindingCount() { return bindingCount; }
    public long getDeviceCount() { return deviceCount; }
    public long getMemberCount() { return memberCount; }
    public long getOrderCount() { return orderCount; }
    public long getPaidOrderCount() { return paidOrderCount; }
    public long getPendingOrderCount() { return pendingOrderCount; }
    public long getLaunchCount() { return launchCount; }
    public long getLoginCount() { return loginCount; }
    public long getPaidAmountCents() { return paidAmountCents; }
}
