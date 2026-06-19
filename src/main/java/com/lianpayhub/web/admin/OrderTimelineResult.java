package com.lianpayhub.web.admin;

import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.user.UserInfo;
import java.util.List;

public class OrderTimelineResult {
    private final PaymentOrder order;
    private final PackageInfo packageInfo;
    private final UserInfo user;
    private final DeviceInfo device;
    private final MemberInfo member;
    private final List<OrderTimelineItem> items;

    public OrderTimelineResult(PaymentOrder order, PackageInfo packageInfo, UserInfo user,
                               DeviceInfo device, MemberInfo member, List<OrderTimelineItem> items) {
        this.order = order;
        this.packageInfo = packageInfo;
        this.user = user;
        this.device = device;
        this.member = member;
        this.items = items;
    }

    public PaymentOrder getOrder() { return order; }
    public PackageInfo getPackageInfo() { return packageInfo; }
    public UserInfo getUser() { return user; }
    public DeviceInfo getDevice() { return device; }
    public MemberInfo getMember() { return member; }
    public List<OrderTimelineItem> getItems() { return items; }
}
