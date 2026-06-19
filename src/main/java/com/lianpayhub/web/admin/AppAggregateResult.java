package com.lianpayhub.web.admin;

import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.payment.PaymentOrder;
import java.util.List;

public class AppAggregateResult {
    private final AppInfo app;
    private final AdminStatsResult stats;
    private final List<PackageInfo> packages;
    private final List<DeviceInfo> recentDevices;
    private final List<MemberInfo> recentMembers;
    private final List<PaymentOrder> recentOrders;
    private final List<LaunchRecord> recentLaunches;
    private final List<AppLoginLog> recentLogins;

    public AppAggregateResult(AppInfo app, AdminStatsResult stats, List<PackageInfo> packages,
                              List<DeviceInfo> recentDevices, List<MemberInfo> recentMembers,
                              List<PaymentOrder> recentOrders, List<LaunchRecord> recentLaunches,
                              List<AppLoginLog> recentLogins) {
        this.app = app;
        this.stats = stats;
        this.packages = packages;
        this.recentDevices = recentDevices;
        this.recentMembers = recentMembers;
        this.recentOrders = recentOrders;
        this.recentLaunches = recentLaunches;
        this.recentLogins = recentLogins;
    }

    public AppInfo getApp() { return app; }
    public AdminStatsResult getStats() { return stats; }
    public List<PackageInfo> getPackages() { return packages; }
    public List<DeviceInfo> getRecentDevices() { return recentDevices; }
    public List<MemberInfo> getRecentMembers() { return recentMembers; }
    public List<PaymentOrder> getRecentOrders() { return recentOrders; }
    public List<LaunchRecord> getRecentLaunches() { return recentLaunches; }
    public List<AppLoginLog> getRecentLogins() { return recentLogins; }
}
