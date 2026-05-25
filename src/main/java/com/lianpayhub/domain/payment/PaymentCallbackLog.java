package com.lianpayhub.domain.payment;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "payment_callback_log", indexes = {
        @Index(name = "idx_payment_callback_order", columnList = "order_id")
})
public class PaymentCallbackLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_channel", nullable = false, length = 32)
    private PayChannel payChannel;

    @Column(name = "pay_provider", nullable = false, length = 64)
    private String payProvider;

    @Column(name = "trade_no", length = 128)
    private String tradeNo;

    @Lob
    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "verify_status", nullable = false, length = 32)
    private CallbackVerifyStatus verifyStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 32)
    private CallbackProcessStatus processStatus;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    protected PaymentCallbackLog() {
    }

    public PaymentCallbackLog(String appId, Long orderId, PayChannel payChannel, String payProvider,
                              String tradeNo, String rawPayload, CallbackVerifyStatus verifyStatus,
                              CallbackProcessStatus processStatus, String errorMessage) {
        this.appId = appId;
        this.orderId = orderId;
        this.payChannel = payChannel;
        this.payProvider = payProvider;
        this.tradeNo = tradeNo;
        this.rawPayload = rawPayload;
        this.verifyStatus = verifyStatus;
        this.processStatus = processStatus;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PayChannel getPayChannel() {
        return payChannel;
    }

    public String getPayProvider() {
        return payProvider;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public CallbackVerifyStatus getVerifyStatus() {
        return verifyStatus;
    }

    public CallbackProcessStatus getProcessStatus() {
        return processStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
