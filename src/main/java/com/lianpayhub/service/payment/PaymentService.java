package com.lianpayhub.service.payment;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.PaymentProperties;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.app.AppType;
import com.lianpayhub.domain.log.PaymentEventLog;
import com.lianpayhub.domain.log.PaymentEventOperatorType;
import com.lianpayhub.domain.log.PaymentEventType;
import com.lianpayhub.domain.member.MemberSubjectType;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.packageinfo.PackageStatus;
import com.lianpayhub.domain.payment.*;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.repository.PaymentCallbackLogRepository;
import com.lianpayhub.repository.PaymentEventLogRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.repository.PaymentRefundRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.member.ActivateMemberCommand;
import com.lianpayhub.service.member.MemberService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final PaymentEventLogRepository paymentEventLogRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final PaymentProviderRegistry providerRegistry;
    private final PaymentChannelConfigService paymentChannelConfigService;
    private final AppService appService;
    private final MemberService memberService;
    private final PaymentProperties paymentProperties;

    public PaymentService(PaymentOrderRepository paymentOrderRepository,
                          PaymentCallbackLogRepository callbackLogRepository,
                          PaymentEventLogRepository paymentEventLogRepository,
                          PaymentRefundRepository paymentRefundRepository,
                          PackageInfoRepository packageInfoRepository,
                          PaymentProviderRegistry providerRegistry,
                          PaymentChannelConfigService paymentChannelConfigService,
                          AppService appService,
                          MemberService memberService,
                          PaymentProperties paymentProperties) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.paymentEventLogRepository = paymentEventLogRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.providerRegistry = providerRegistry;
        this.paymentChannelConfigService = paymentChannelConfigService;
        this.appService = appService;
        this.memberService = memberService;
        this.paymentProperties = paymentProperties;
    }

    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        AppInfo app = appService.requireEnabledApp(command.appId());
        PackageInfo packageInfo = packageInfoRepository.findById(command.packageId())
                .filter(item -> item.getAppId().equals(command.appId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在"));
        if (packageInfo.getStatus() != PackageStatus.ENABLED) {
            throw new BusinessException(ErrorCode.CONFLICT, "套餐已下架");
        }

        if (app.getAppType() == AppType.DEVICE_ONLY && command.deviceId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备码会员订单必须提供 deviceId");
        }
        if (app.getAppType() != AppType.DEVICE_ONLY && command.userId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号会员订单必须提供 userId");
        }

        PayChannel payChannel = resolvePayChannel(command.appId(), command.payChannel());
        PaymentProvider provider = providerRegistry.require(payChannel);
        PaymentChannelConfig channelConfig = paymentChannelConfigService
                .findByAppAndChannel(command.appId(), payChannel)
                .orElse(null);
        if (channelConfig != null && !channelConfig.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "支付渠道配置已停用");
        }
        String payProvider = channelConfig == null ? provider.providerCode() : channelConfig.getProviderCode();
        String orderNo = "LFP" + UUID.randomUUID().toString().replace("-", "");
        PaymentOrder order = paymentOrderRepository.save(new PaymentOrder(
                command.appId(),
                command.userId(),
                command.deviceId(),
                command.packageId(),
                orderNo,
                packageInfo.getPriceCents(),
                provider.payChannel(),
                payProvider,
                LocalDateTime.now().plusMinutes(orderExpireMinutes())
        ));
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.ORDER_CREATED, provider.payChannel(),
                payProvider, order.getAmountCents(), null, PaymentEventOperatorType.SYSTEM,
                null, "{\"orderNo\":\"" + order.getOrderNo() + "\",\"expireAt\":\"" + order.getExpireAt() + "\"}"
        ));
        String runtimeDomain = currentDomain();
        PaymentChannelConfig runtimeChannelConfig = resolveRuntimeDomain(channelConfig, runtimeDomain);
        String runtimeReturnUrl = replaceDomain(command.returnUrl(), runtimeDomain);
        PaymentCreateResult payment = provider.createPayment(new PaymentCreateContext(
                order.getOrderNo(),
                order.getAmountCents(),
                packageInfo.getPackageName(),
                command.payMode(),
                runtimeReturnUrl,
                runtimeChannelConfig
        ));
        return new CreateOrderResult(order.getOrderNo(), order.getAmountCents(),
                enrichPaymentParams(payment.paymentParams(), runtimeChannelConfig, payProvider));
    }

    @Transactional(readOnly = true)
    public List<PayChannel> listEnabledPayChannels() {
        return paymentChannelConfigService.listEnabledChannels();
    }

    private PayChannel resolvePayChannel(String appId, PayChannel requested) {
        if (requested != null) {
            return requested;
        }
        return PayChannel.ALIPAY;
    }

    private Map<String, Object> enrichPaymentParams(Map<String, Object> paymentParams,
                                                    PaymentChannelConfig channelConfig,
                                                    String payProvider) {
        Map<String, Object> params = new HashMap<>();
        if (paymentParams != null) {
            params.putAll(paymentParams);
        }
        params.put("provider", payProvider);
        params.put("configured", channelConfig != null);
        if (channelConfig != null) {
            params.put("merchantId", channelConfig.getMerchantId());
            params.put("channelAppId", channelConfig.getChannelAppId());
            params.put("notifyUrl", channelConfig.getNotifyUrl());
            params.put("configJson", channelConfig.getConfigJson());
        }
        return params;
    }

    private PaymentChannelConfig resolveRuntimeDomain(PaymentChannelConfig config, String domain) {
        if (config == null) {
            return null;
        }
        if (domain == null) {
            return config;
        }
        return new PaymentChannelConfig(
                config.getAppId(),
                config.getPayChannel(),
                config.getProviderCode(),
                config.getMerchantId(),
                config.getChannelAppId(),
                replaceDomain(config.getNotifyUrl(), domain),
                replaceDomain(config.getConfigJson(), domain),
                config.getCredentialJson()
        );
    }

    private String replaceDomain(String value, String domain) {
        if (value == null || domain == null) {
            return value;
        }
        return value.replace("${domain}", domain);
    }

    private String currentDomain() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwardedHost = firstHeader(request, "X-Forwarded-Host", "X-Original-Host");
        String forwardedProto = firstHeader(request, "X-Forwarded-Proto");
        if (forwardedHost != null) {
            return (forwardedProto == null ? request.getScheme() : forwardedProto.split(",")[0].trim()) + "://" + forwardedHost.split(",")[0].trim();
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    @Transactional
    public void markPaidForTest(String orderNo, String tradeNo) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        markPaidManually(order, tradeNo, null);
    }

    @Transactional
    public void markPaidByAdmin(Long orderId, String tradeNo, String operatorId) {
        PaymentOrder order = paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        markPaidManually(order, tradeNo, operatorId);
    }

    private void markPaidManually(PaymentOrder order, String tradeNo, String operatorId) {
        if (order.isPaid()) {
            return;
        }
        closeIfExpired(order, "订单已过期，不能手动标记支付");
        PackageInfo packageInfo = packageInfoRepository.findById(order.getPackageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在"));
        order.markPaid(tradeNo, tradeNo, "{}");
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.PAYMENT_SUCCESS, PayChannel.OTHER,
                "manual", order.getAmountCents(), tradeNo, PaymentEventOperatorType.ADMIN,
                operatorId, "{\"manual\":true}"
        ));
        memberService.activateOrExtend(new ActivateMemberCommand(
                order.getAppId(),
                order.getDeviceId() == null ? MemberSubjectType.USER : MemberSubjectType.DEVICE,
                order.getUserId(),
                order.getDeviceId(),
                order.getPackageId(),
                packageInfo.getDurationDays(),
                order.getId()
        ));
        callbackLogRepository.save(new PaymentCallbackLog(
                order.getAppId(), order.getId(), PayChannel.OTHER, "manual", tradeNo, "{}",
                CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.SUCCESS, null
        ));
    }

    @Transactional
    public PaymentNotifyResult handlePaymentNotify(PayChannel payChannel, String rawPayload) {
        PaymentProvider provider = providerRegistry.require(payChannel);
        PaymentCallbackResult callback = provider.parseCallback(rawPayload);
        if (callback.orderNo() == null || callback.orderNo().trim().isEmpty()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    "UNKNOWN", null, payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.FAILED, CallbackProcessStatus.FAILED,
                    "支付回调缺少订单号"
            ));
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付回调缺少订单号");
        }

        PaymentOrder order = paymentOrderRepository.findByOrderNo(callback.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        PaymentChannelConfig channelConfig = paymentChannelConfigService.findByAppAndChannel(order.getAppId(), payChannel).orElse(null);
        if (channelConfig != null) {
            callback = provider.parseCallback(new PaymentChannelContext(channelConfig), rawPayload);
        }
        if (!callback.verified()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    order.getAppId(), order.getId(), payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.FAILED, CallbackProcessStatus.FAILED,
                    "支付回调验签失败或支付状态未成功"
            ));
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付回调验签失败或支付状态未成功");
        }
        if (order.isPaid()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    order.getAppId(), order.getId(), payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.IGNORED,
                    "订单已支付，重复回调已忽略"
            ));
            return new PaymentNotifyResult(order.getOrderNo(), callback.tradeNo(), false, "duplicate");
        }
        closeIfExpiredForCallback(order, payChannel, provider.providerCode(), callback);
        if (!order.isPending()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    order.getAppId(), order.getId(), payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.FAILED,
                    "当前订单状态不允许支付成功"
            ));
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不允许支付成功");
        }

        PackageInfo packageInfo = packageInfoRepository.findById(order.getPackageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在"));
        order.markPaid(callback.tradeNo(), callback.channelOrderNo(), callback.rawPayload());
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.PAYMENT_SUCCESS, payChannel,
                provider.providerCode(), order.getAmountCents(), callback.tradeNo(),
                PaymentEventOperatorType.SYSTEM, null, callback.rawPayload()
        ));
        memberService.activateOrExtend(new ActivateMemberCommand(
                order.getAppId(),
                order.getDeviceId() == null ? MemberSubjectType.USER : MemberSubjectType.DEVICE,
                order.getUserId(),
                order.getDeviceId(),
                order.getPackageId(),
                packageInfo.getDurationDays(),
                order.getId()
        ));
        callbackLogRepository.save(new PaymentCallbackLog(
                order.getAppId(), order.getId(), payChannel, provider.providerCode(), callback.tradeNo(),
                callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.SUCCESS, null
        ));
        return new PaymentNotifyResult(order.getOrderNo(), callback.tradeNo(), true, "success");
    }

    @Transactional
    public void closeOrderByAdmin(Long orderId, String reason, String operatorId) {
        PaymentOrder order = paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        closePendingOrder(order, safeCloseReason(reason, "管理员关闭"), PaymentEventOperatorType.ADMIN, operatorId);
    }

    @Transactional
    public int closeExpiredOrders() {
        List<PaymentOrder> orders = paymentOrderRepository
                .findTop100ByPayStatusAndExpireAtBeforeOrderByExpireAtAsc(PayStatus.PENDING, LocalDateTime.now());
        for (PaymentOrder order : orders) {
            closePendingOrder(order, "订单超时自动关闭", PaymentEventOperatorType.SYSTEM, null);
        }
        return orders.size();
    }

    @Transactional
    public RefundResult requestRefund(RefundCommand command) {
        PaymentOrder order = paymentOrderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        Long pendingAmount = paymentRefundRepository.sumAmountCentsByOrderIdAndStatus(
                order.getId(), RefundStatus.PENDING);
        int reservedAmount = pendingAmount == null ? 0 : pendingAmount.intValue();
        if (!order.canRefund(command.amountCents())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态或退款金额不允许退款");
        }
        if (order.getRefundedAmountCents() + reservedAmount + command.amountCents() > order.getAmountCents()) {
            throw new BusinessException(ErrorCode.CONFLICT, "存在待处理退款，剩余可退金额不足");
        }

        String refundNo = "LFR" + UUID.randomUUID().toString().replace("-", "");
        PaymentRefund refund = paymentRefundRepository.save(new PaymentRefund(
                order.getAppId(),
                order.getId(),
                refundNo,
                command.amountCents(),
                command.reason()
        ));
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.REFUND_CREATED, order.getPayChannel(),
                order.getPayProvider(), command.amountCents(), order.getTradeNo(),
                PaymentEventOperatorType.ADMIN, command.operatorId(),
                "{\"refundNo\":\"" + refund.getRefundNo() + "\"}"
        ));
        return new RefundResult(refund.getId(), refund.getRefundNo(), refund.getAmountCents(), refund.getStatus());
    }

    @Transactional(readOnly = true)
    public PaymentOrder publicOrder(String orderNo) {
        return paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    @Transactional(readOnly = true)
    public ChannelOrderResult queryChannelOrder(Long orderId) {
        PaymentOrder order = paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        PaymentProvider provider = providerRegistry.require(order.getPayChannel());
        PaymentChannelConfig config = paymentChannelConfigService.findByAppAndChannel(order.getAppId(), order.getPayChannel())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "支付渠道未配置"));
        return provider.queryOrder(new PaymentChannelContext(config), order.getOrderNo(), order.getTradeNo());
    }

    @Transactional
    public RefundResult refundToChannel(Long refundId, String operatorId) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款单不存在"));
        PaymentOrder order = paymentOrderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (!refund.isPending()) {
            throw new BusinessException(ErrorCode.CONFLICT, "退款单已处理，不能重复退款");
        }
        PaymentProvider provider = providerRegistry.require(order.getPayChannel());
        PaymentChannelConfig config = paymentChannelConfigService.findByAppAndChannel(order.getAppId(), order.getPayChannel())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "支付渠道未配置"));
        ChannelRefundResult result = provider.refund(new PaymentChannelContext(config), order.getOrderNo(), order.getTradeNo(),
                refund.getRefundNo(), refund.getAmountCents(), refund.getReason());
        if (!result.isSupported()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, result.getMessage());
        }
        if (!result.isSuccess()) {
            refund.markFailed();
            paymentEventLogRepository.save(new PaymentEventLog(
                    order.getAppId(), order.getId(), PaymentEventType.REFUND_FAILED, order.getPayChannel(),
                    order.getPayProvider(), refund.getAmountCents(), order.getTradeNo(),
                    PaymentEventOperatorType.ADMIN, operatorId,
                    "{\"refundNo\":\"" + refund.getRefundNo() + "\",\"message\":\"" + escapeJson(result.getMessage()) + "\"}"
            ));
            return new RefundResult(refund.getId(), refund.getRefundNo(), refund.getAmountCents(), refund.getStatus());
        }
        return markRefundSuccess(refundId, result.getChannelRefundNo(), operatorId);
    }

    private void closeIfExpired(PaymentOrder order, String message) {
        if (order.isPending() && order.isExpired(LocalDateTime.now())) {
            closePendingOrder(order, "订单超时自动关闭", PaymentEventOperatorType.SYSTEM, null);
            throw new BusinessException(ErrorCode.CONFLICT, message);
        }
    }

    private void closeIfExpiredForCallback(PaymentOrder order, PayChannel payChannel, String payProvider,
                                           PaymentCallbackResult callback) {
        if (!order.isPending() || !order.isExpired(LocalDateTime.now())) {
            return;
        }
        closePendingOrder(order, "订单超时自动关闭", PaymentEventOperatorType.SYSTEM, null);
        callbackLogRepository.save(new PaymentCallbackLog(
                order.getAppId(), order.getId(), payChannel, payProvider, callback.tradeNo(),
                callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.FAILED,
                "订单已超时关闭，支付回调不再入账"
        ));
        throw new BusinessException(ErrorCode.CONFLICT, "订单已超时关闭，支付回调不再入账");
    }

    private void closePendingOrder(PaymentOrder order, String reason, PaymentEventOperatorType operatorType,
                                   String operatorId) {
        order.close(reason);
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.ORDER_CLOSED, order.getPayChannel(),
                order.getPayProvider(), order.getAmountCents(), order.getTradeNo(), operatorType, operatorId,
                "{\"reason\":\"" + escapeJson(reason) + "\"}"
        ));
    }

    private String safeCloseReason(String reason, String fallback) {
        if (reason == null || reason.trim().isEmpty()) {
            return fallback;
        }
        String value = reason.trim();
        return value.length() > 512 ? value.substring(0, 512) : value;
    }

    private int orderExpireMinutes() {
        Integer value = paymentProperties.getOrderExpireMinutes();
        return value == null || value < 1 ? 30 : value;
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Transactional
    public RefundResult markRefundSuccess(Long refundId, String channelRefundNo, String operatorId) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款单不存在"));
        PaymentOrder order = paymentOrderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (!refund.isPending()) {
            throw new BusinessException(ErrorCode.CONFLICT, "退款单已处理，不能重复确认");
        }
        refund.markSuccess(channelRefundNo);
        order.markRefunded(refund.getAmountCents());
        memberService.handleRefund(order.getAppId(), order.getUserId(), order.getDeviceId(), order.getId());
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.REFUND_SUCCESS, order.getPayChannel(),
                order.getPayProvider(), refund.getAmountCents(), order.getTradeNo(),
                PaymentEventOperatorType.ADMIN, operatorId,
                "{\"refundNo\":\"" + refund.getRefundNo() + "\",\"channelRefundNo\":\"" + channelRefundNo + "\"}"
        ));
        return new RefundResult(refund.getId(), refund.getRefundNo(), refund.getAmountCents(), refund.getStatus());
    }

    @Transactional
    public RefundResult markRefundFailed(Long refundId, String operatorId) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款单不存在"));
        PaymentOrder order = paymentOrderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (!refund.isPending()) {
            throw new BusinessException(ErrorCode.CONFLICT, "退款单已处理，不能重复确认");
        }
        refund.markFailed();
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.REFUND_FAILED, order.getPayChannel(),
                order.getPayProvider(), refund.getAmountCents(), order.getTradeNo(),
                PaymentEventOperatorType.ADMIN, operatorId,
                "{\"refundNo\":\"" + refund.getRefundNo() + "\"}"
        ));
        return new RefundResult(refund.getId(), refund.getRefundNo(), refund.getAmountCents(), refund.getStatus());
    }
}
