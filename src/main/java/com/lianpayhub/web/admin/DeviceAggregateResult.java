package com.lianpayhub.web.admin;

import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.device.DeviceCodeChangeLog;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.user.UserInfo;
import java.util.List;

public class DeviceAggregateResult {
    private final DeviceInfo device;
    private final AppInfo app;
    private final UserInfo user;
    private final MemberInfo deviceMember;
    private final AdminStatsResult stats;
    private final List<PaymentOrder> recentOrders;
    private final List<LaunchRecord> recentLaunches;
    private final List<AppLoginLog> recentLogins;
    private final List<DeviceCodeChangeLog> recentDeviceCodeChanges;
    private final DeviceUsageStatsResult usageStats;

    public DeviceAggregateResult(DeviceInfo device, AppInfo app, UserInfo user, MemberInfo deviceMember,
                                 AdminStatsResult stats, List<PaymentOrder> recentOrders,
                                 List<LaunchRecord> recentLaunches, List<AppLoginLog> recentLogins,
                                 List<DeviceCodeChangeLog> recentDeviceCodeChanges,
                                 DeviceUsageStatsResult usageStats) {
        this.device = device;
        this.app = app;
        this.user = user;
        this.deviceMember = deviceMember;
        this.stats = stats;
        this.recentOrders = recentOrders;
        this.recentLaunches = recentLaunches;
        this.recentLogins = recentLogins;
        this.recentDeviceCodeChanges = recentDeviceCodeChanges;
        this.usageStats = usageStats;
    }

    public DeviceInfo getDevice() { return device; }
    public AppInfo getApp() { return app; }
    public UserInfo getUser() { return user; }
    public MemberInfo getDeviceMember() { return deviceMember; }
    public AdminStatsResult getStats() { return stats; }
    public List<PaymentOrder> getRecentOrders() { return recentOrders; }
    public List<LaunchRecord> getRecentLaunches() { return recentLaunches; }
    public List<AppLoginLog> getRecentLogins() { return recentLogins; }
    public List<DeviceCodeChangeLog> getRecentDeviceCodeChanges() { return recentDeviceCodeChanges; }
    public DeviceUsageStatsResult getUsageStats() { return usageStats; }
}
