package com.lianpayhub.service.admin;

import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.storage.UserFile;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import java.util.List;

public class AdminUserProfileResult {
    private final UserInfo user;
    private final Stats stats;
    private final List<UserAppBinding> bindings;
    private final List<DeviceInfo> recentDevices;
    private final List<AppLoginLog> recentLogins;
    private final List<LaunchRecord> recentLaunches;
    private final List<PaymentOrder> recentOrders;
    private final List<MemberInfo> recentMembers;
    private final List<UserFile> recentFiles;

    public AdminUserProfileResult(UserInfo user, Stats stats,
                                  List<UserAppBinding> bindings,
                                  List<DeviceInfo> recentDevices,
                                  List<AppLoginLog> recentLogins,
                                  List<LaunchRecord> recentLaunches,
                                  List<PaymentOrder> recentOrders,
                                  List<MemberInfo> recentMembers,
                                  List<UserFile> recentFiles) {
        this.user = user;
        this.stats = stats;
        this.bindings = bindings;
        this.recentDevices = recentDevices;
        this.recentLogins = recentLogins;
        this.recentLaunches = recentLaunches;
        this.recentOrders = recentOrders;
        this.recentMembers = recentMembers;
        this.recentFiles = recentFiles;
    }

    public UserInfo getUser() { return user; }
    public Stats getStats() { return stats; }
    public List<UserAppBinding> getBindings() { return bindings; }
    public List<DeviceInfo> getRecentDevices() { return recentDevices; }
    public List<AppLoginLog> getRecentLogins() { return recentLogins; }
    public List<LaunchRecord> getRecentLaunches() { return recentLaunches; }
    public List<PaymentOrder> getRecentOrders() { return recentOrders; }
    public List<MemberInfo> getRecentMembers() { return recentMembers; }
    public List<UserFile> getRecentFiles() { return recentFiles; }

    public static class Stats {
        private final long bindingCount;
        private final long deviceCount;
        private final long loginCount;
        private final long launchCount;
        private final long orderCount;
        private final long paidOrderCount;
        private final long paidAmountCents;
        private final long memberCount;
        private final long fileCount;
        private final long usedBytes;

        public Stats(long bindingCount, long deviceCount, long loginCount, long launchCount,
                     long orderCount, long paidOrderCount, long paidAmountCents, long memberCount,
                     long fileCount, long usedBytes) {
            this.bindingCount = bindingCount;
            this.deviceCount = deviceCount;
            this.loginCount = loginCount;
            this.launchCount = launchCount;
            this.orderCount = orderCount;
            this.paidOrderCount = paidOrderCount;
            this.paidAmountCents = paidAmountCents;
            this.memberCount = memberCount;
            this.fileCount = fileCount;
            this.usedBytes = usedBytes;
        }

        public long getBindingCount() { return bindingCount; }
        public long getDeviceCount() { return deviceCount; }
        public long getLoginCount() { return loginCount; }
        public long getLaunchCount() { return launchCount; }
        public long getOrderCount() { return orderCount; }
        public long getPaidOrderCount() { return paidOrderCount; }
        public long getPaidAmountCents() { return paidAmountCents; }
        public long getMemberCount() { return memberCount; }
        public long getFileCount() { return fileCount; }
        public long getUsedBytes() { return usedBytes; }
    }
}
