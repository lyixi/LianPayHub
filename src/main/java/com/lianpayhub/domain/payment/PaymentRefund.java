package com.lianpayhub.domain.payment;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_refund", indexes = {
        @Index(name = "idx_payment_refund_no", columnList = "refund_no", unique = true),
        @Index(name = "idx_payment_refund_order", columnList = "order_id"),
        @Index(name = "idx_payment_refund_app", columnList = "app_id")
})
public class PaymentRefund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "refund_no", nullable = false, length = 64, unique = true)
    private String refundNo;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "reason", length = 512)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "channel_refund_no", length = 128)
    private String channelRefundNo;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected PaymentRefund() {
    }

    public PaymentRefund(String appId, Long orderId, String refundNo, Integer amountCents, String reason) {
        this.appId = appId;
        this.orderId = orderId;
        this.refundNo = refundNo;
        this.amountCents = amountCents;
        this.reason = reason;
    }

    public boolean isPending() {
        return status == RefundStatus.PENDING;
    }

    public void markSuccess(String channelRefundNo) {
        this.status = RefundStatus.SUCCESS;
        this.channelRefundNo = channelRefundNo;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = RefundStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public Long getOrderId() { return orderId; }
    public String getRefundNo() { return refundNo; }
    public Integer getAmountCents() { return amountCents; }
    public String getReason() { return reason; }
    public RefundStatus getStatus() { return status; }
    public String getChannelRefundNo() { return channelRefundNo; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
