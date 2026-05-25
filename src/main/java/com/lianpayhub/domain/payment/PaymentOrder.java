package com.lianpayhub.domain.payment;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order", indexes = {
        @Index(name = "idx_payment_order_no", columnList = "order_no", unique = true),
        @Index(name = "idx_payment_order_app", columnList = "app_id"),
        @Index(name = "idx_payment_order_user", columnList = "user_id"),
        @Index(name = "idx_payment_order_device", columnList = "device_id")
})
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "order_no", nullable = false, length = 64, unique = true)
    private String orderNo;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", nullable = false, length = 32)
    private PayChannel payChannel;

    @Column(name = "pay_provider", nullable = false, length = 64)
    private String payProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status", nullable = false, length = 32)
    private PayStatus payStatus = PayStatus.PENDING;

    @Column(name = "trade_no", length = 128)
    private String tradeNo;

    @Column(name = "channel_order_no", length = 128)
    private String channelOrderNo;

    @Lob
    @Column(name = "callback_data")
    private String callbackData;

    @Column(name = "callback_count", nullable = false)
    private Integer callbackCount = 0;

    @Column(name = "refunded_amount_cents", nullable = false)
    private Integer refundedAmountCents = 0;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    protected PaymentOrder() {
    }

    public PaymentOrder(String appId, Long userId, Long deviceId, Long packageId, String orderNo,
                        Integer amountCents, PayChannel payChannel, String payProvider) {
        this.appId = appId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.packageId = packageId;
        this.orderNo = orderNo;
        this.amountCents = amountCents;
        this.payChannel = payChannel;
        this.payProvider = payProvider;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public PayChannel getPayChannel() {
        return payChannel;
    }

    public String getPayProvider() {
        return payProvider;
    }

    public PayStatus getPayStatus() {
        return payStatus;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public Integer getCallbackCount() {
        return callbackCount;
    }

    public Integer getRefundedAmountCents() {
        return refundedAmountCents;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public boolean isPaid() {
        return payStatus == PayStatus.PAID;
    }

    public boolean canRefund(Integer amountCents) {
        return (payStatus == PayStatus.PAID || payStatus == PayStatus.PARTIAL_REFUNDED)
                && amountCents != null
                && amountCents > 0
                && refundedAmountCents + amountCents <= this.amountCents;
    }

    public void markRefunded(Integer amountCents) {
        if (!canRefund(amountCents)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态或退款金额不允许退款");
        }
        this.refundedAmountCents += amountCents;
        this.payStatus = this.refundedAmountCents.equals(this.amountCents)
                ? PayStatus.REFUNDED
                : PayStatus.PARTIAL_REFUNDED;
    }

    public void markPaid(String tradeNo, String channelOrderNo, String callbackData) {
        if (payStatus == PayStatus.PAID) {
            this.callbackCount++;
            return;
        }
        if (payStatus != PayStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不允许支付成功");
        }
        this.tradeNo = tradeNo;
        this.channelOrderNo = channelOrderNo;
        this.callbackData = callbackData;
        this.callbackCount++;
        this.payStatus = PayStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}
