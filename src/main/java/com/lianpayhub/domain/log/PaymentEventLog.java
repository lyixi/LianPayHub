package com.lianpayhub.domain.log;

import com.lianpayhub.domain.payment.PayChannel;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_event_log", indexes = {
        @Index(name = "idx_payment_event_app_time", columnList = "app_id,created_at"),
        @Index(name = "idx_payment_event_order", columnList = "order_id")
})
public class PaymentEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private PaymentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", length = 32)
    private PayChannel payChannel;

    @Column(name = "pay_provider", length = 64)
    private String payProvider;

    @Column(name = "amount_cents")
    private Integer amountCents;

    @Column(name = "trade_no", length = 128)
    private String tradeNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_type", nullable = false, length = 32)
    private PaymentEventOperatorType operatorType;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Lob
    @Column(name = "event_data")
    private String eventData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PaymentEventLog() {
    }

    public PaymentEventLog(String appId, Long orderId, PaymentEventType eventType, PayChannel payChannel,
                           String payProvider, Integer amountCents, String tradeNo,
                           PaymentEventOperatorType operatorType, String operatorId, String eventData) {
        this.appId = appId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.payChannel = payChannel;
        this.payProvider = payProvider;
        this.amountCents = amountCents;
        this.tradeNo = tradeNo;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.eventData = eventData;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public Long getOrderId() { return orderId; }
    public PaymentEventType getEventType() { return eventType; }
    public PayChannel getPayChannel() { return payChannel; }
    public String getPayProvider() { return payProvider; }
    public Integer getAmountCents() { return amountCents; }
    public String getTradeNo() { return tradeNo; }
    public PaymentEventOperatorType getOperatorType() { return operatorType; }
    public String getOperatorId() { return operatorId; }
    public String getEventData() { return eventData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
