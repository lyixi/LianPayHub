package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.member.MemberSubjectType;
import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.*;
import com.lianpayhub.web.admin.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAggregateService {

    private final AppInfoRepository appInfoRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceCodeChangeLogRepository deviceCodeChangeLogRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final PaymentEventLogRepository eventLogRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final UserInfoRepository userInfoRepository;

    public AdminAggregateService(AppInfoRepository appInfoRepository,
                                 PackageInfoRepository packageInfoRepository,
                                 UserAppBindingRepository userAppBindingRepository,
                                 DeviceInfoRepository deviceInfoRepository,
                                 DeviceCodeChangeLogRepository deviceCodeChangeLogRepository,
                                 MemberInfoRepository memberInfoRepository,
                                 PaymentOrderRepository paymentOrderRepository,
                                 PaymentRefundRepository paymentRefundRepository,
                                 PaymentCallbackLogRepository callbackLogRepository,
                                 PaymentEventLogRepository eventLogRepository,
                                 LaunchRecordRepository launchRecordRepository,
                                 AppLoginLogRepository appLoginLogRepository,
                                 UserInfoRepository userInfoRepository) {
        this.appInfoRepository = appInfoRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.userAppBindingRepository = userAppBindingRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceCodeChangeLogRepository = deviceCodeChangeLogRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.eventLogRepository = eventLogRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.userInfoRepository = userInfoRepository;
    }

    @Transactional(readOnly = true)
    public AppAggregateResult appAggregate(Long id) {
        AppInfo app = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        String appId = app.getAppId();
        AdminStatsResult stats = new AdminStatsResult(
                packageInfoRepository.countByAppId(appId),
                userAppBindingRepository.countByAppId(appId),
                deviceInfoRepository.countByAppId(appId),
                memberInfoRepository.countByAppId(appId),
                paymentOrderRepository.countByAppId(appId),
                paymentOrderRepository.countByAppIdAndPayStatus(appId, PayStatus.PAID),
                paymentOrderRepository.countByAppIdAndPayStatus(appId, PayStatus.PENDING),
                launchRecordRepository.countByAppId(appId),
                appLoginLogRepository.countByAppId(appId),
                safeLong(paymentOrderRepository.sumAmountCentsByAppIdAndPayStatus(appId, PayStatus.PAID))
        );
        return new AppAggregateResult(
                app,
                stats,
                packageInfoRepository.findByAppId(appId),
                deviceInfoRepository.findTop10ByAppIdOrderByIdDesc(appId),
                memberInfoRepository.findTop10ByAppIdOrderByIdDesc(appId),
                paymentOrderRepository.findTop10ByAppIdOrderByIdDesc(appId),
                launchRecordRepository.findTop10ByAppIdOrderByIdDesc(appId),
                appLoginLogRepository.findTop10ByAppIdOrderByIdDesc(appId)
        );
    }

    @Transactional(readOnly = true)
    public DeviceAggregateResult deviceAggregate(Long id) {
        DeviceInfo device = deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在"));
        AppInfo app = appInfoRepository.findByAppId(device.getAppId()).orElse(null);
        UserInfo user = device.getUserId() == null ? null : userInfoRepository.findById(device.getUserId()).orElse(null);
        MemberInfo deviceMember = memberInfoRepository.findFirstByAppIdAndMemberSubjectTypeAndDeviceIdOrderByExpireAtDesc(
                device.getAppId(), MemberSubjectType.DEVICE, device.getId()).orElse(null);
        AdminStatsResult stats = new AdminStatsResult(
                0,
                0,
                1,
                deviceMember == null ? 0 : 1,
                paymentOrderRepository.countByDeviceId(device.getId()),
                paymentOrderRepository.countByDeviceIdAndPayStatus(device.getId(), PayStatus.PAID),
                paymentOrderRepository.countByDeviceIdAndPayStatus(device.getId(), PayStatus.PENDING),
                launchRecordRepository.countByDeviceId(device.getId()),
                appLoginLogRepository.countByDeviceId(device.getId()),
                safeLong(paymentOrderRepository.sumAmountCentsByDeviceIdAndPayStatus(device.getId(), PayStatus.PAID))
        );
        return new DeviceAggregateResult(
                device,
                app,
                user,
                deviceMember,
                stats,
                paymentOrderRepository.findTop10ByDeviceIdOrderByIdDesc(device.getId()),
                launchRecordRepository.findTop10ByDeviceIdOrderByIdDesc(device.getId()),
                appLoginLogRepository.findTop10ByDeviceIdOrderByIdDesc(device.getId()),
                deviceCodeChangeLogRepository.findTop10ByDeviceIdOrderByIdDesc(device.getId()),
                new DeviceUsageStatsResult(
                        safeLong(launchRecordRepository.sumDurationSecondsByDeviceId(device.getId())),
                        Math.round(safeDouble(launchRecordRepository.avgDurationSecondsByDeviceId(device.getId())))
                )
        );
    }

    @Transactional(readOnly = true)
    public OrderTimelineResult orderTimeline(Long id) {
        PaymentOrder order = paymentOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        List<OrderTimelineItem> items = new ArrayList<>();
        add(items, "ORDER", order.getId(), "订单创建", order.getOrderNo(), order.getPayStatus().name(), order.getCreatedAt(), order.getAmountCents());
        add(items, "ORDER", order.getId(), "支付成功", order.getTradeNo(), "PAID", order.getPaidAt(), order.getAmountCents());
        add(items, "ORDER", order.getId(), "订单关闭", order.getCloseReason(), "CANCELLED", order.getClosedAt(), order.getAmountCents());
        PageRequest recent = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "id"));
        eventLogRepository.findByOrderId(id, recent).forEach(e -> add(items, "PAYMENT_EVENT", e.getId(), labelOf(e.getEventType().name()), e.getEventData(), e.getEventType().name(), e.getCreatedAt(), e.getAmountCents()));
        callbackLogRepository.findByOrderId(id, recent).forEach(c -> add(items, "CALLBACK", c.getId(), "支付回调", c.getErrorMessage(), c.getProcessStatus().name(), c.getCreatedAt(), null));
        paymentRefundRepository.findByOrderId(id, recent).forEach(r -> addRefundItems(items, r));
        MemberInfo member = memberInfoRepository.findByOrderId(id).orElse(null);
        if (member != null) {
            add(items, "MEMBER", member.getId(), "会员生效", "到期时间 " + member.getExpireAt(), member.getStatus().name(), member.getStartAt(), null);
        }
        items.sort(Comparator.comparing(OrderTimelineItem::getHappenedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return new OrderTimelineResult(
                order,
                packageInfoRepository.findById(order.getPackageId()).orElse(null),
                order.getUserId() == null ? null : userInfoRepository.findById(order.getUserId()).orElse(null),
                order.getDeviceId() == null ? null : deviceInfoRepository.findById(order.getDeviceId()).orElse(null),
                member,
                items
        );
    }

    private void addRefundItems(List<OrderTimelineItem> items, PaymentRefund refund) {
        add(items, "REFUND", refund.getId(), "退款申请", refund.getReason(), refund.getStatus().name(), refund.getCreatedAt(), refund.getAmountCents());
        add(items, "REFUND", refund.getId(), "退款处理", refund.getChannelRefundNo(), refund.getStatus().name(), refund.getProcessedAt(), refund.getAmountCents());
    }

    private void add(List<OrderTimelineItem> items, String sourceType, Long sourceId, String title, String description,
                     String status, LocalDateTime happenedAt, Integer amountCents) {
        if (happenedAt == null) {
            return;
        }
        items.add(new OrderTimelineItem(sourceType, sourceId, title, description, status, happenedAt, amountCents));
    }

    private String labelOf(String eventType) {
        if (eventType == null) return "支付事件";
        switch (eventType) {
            case "ORDER_CREATED": return "订单创建事件";
            case "PAYMENT_SUCCESS": return "支付成功事件";
            case "ORDER_CLOSED": return "订单关闭事件";
            case "REFUND_CREATED": return "退款创建事件";
            case "REFUND_SUCCESS": return "退款成功事件";
            case "REFUND_FAILED": return "退款失败事件";
            default: return eventType;
        }
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }
}
