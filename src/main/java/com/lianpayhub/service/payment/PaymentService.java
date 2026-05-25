package com.lianpayhub.service.payment;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PaymentService(PaymentOrderRepository paymentOrderRepository,
                          PaymentCallbackLogRepository callbackLogRepository,
                          PaymentEventLogRepository paymentEventLogRepository,
                          PaymentRefundRepository paymentRefundRepository,
                          PackageInfoRepository packageInfoRepository,
                          PaymentProviderRegistry providerRegistry,
                          PaymentChannelConfigService paymentChannelConfigService,
                          AppService appService,
                          MemberService memberService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.paymentEventLogRepository = paymentEventLogRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.providerRegistry = providerRegistry;
        this.paymentChannelConfigService = paymentChannelConfigService;
        this.appService = appService;
        this.memberService = memberService;
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

        PaymentProvider provider = providerRegistry.require(command.payChannel());
        PaymentChannelConfig channelConfig = paymentChannelConfigService
                .findByAppAndChannel(command.appId(), command.payChannel())
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
                payProvider
        ));
        paymentEventLogRepository.save(new PaymentEventLog(
                order.getAppId(), order.getId(), PaymentEventType.ORDER_CREATED, provider.payChannel(),
                payProvider, order.getAmountCents(), null, PaymentEventOperatorType.SYSTEM,
                null, "{\"orderNo\":\"" + order.getOrderNo() + "\"}"
        ));
        PaymentCreateResult payment = provider.createPayment(new PaymentCreateContext(
                order.getOrderNo(),
                order.getAmountCents(),
                packageInfo.getPackageName()
        ));
        return new CreateOrderResult(order.getOrderNo(), order.getAmountCents(),
                enrichPaymentParams(payment.paymentParams(), channelConfig, payProvider));
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
        if (!callback.verified()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    "UNKNOWN", null, payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.FAILED, CallbackProcessStatus.FAILED,
                    "支付回调验签失败或支付状态未成功"
            ));
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付回调验签失败或支付状态未成功");
        }
        if (callback.orderNo() == null || callback.orderNo().trim().isEmpty()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    "UNKNOWN", null, payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.FAILED,
                    "支付回调缺少订单号"
            ));
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付回调缺少订单号");
        }

        PaymentOrder order = paymentOrderRepository.findByOrderNo(callback.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        if (order.isPaid()) {
            callbackLogRepository.save(new PaymentCallbackLog(
                    order.getAppId(), order.getId(), payChannel, provider.providerCode(), callback.tradeNo(),
                    callback.rawPayload(), CallbackVerifyStatus.VERIFIED, CallbackProcessStatus.IGNORED,
                    "订单已支付，重复回调已忽略"
            ));
            return new PaymentNotifyResult(order.getOrderNo(), callback.tradeNo(), false, "duplicate");
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
