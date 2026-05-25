package com.lianpayhub.service.payment;

public class PaymentCallbackResult {
    private final boolean verified;
    private final String orderNo;
    private final String tradeNo;
    private final String channelOrderNo;
    private final String rawPayload;

    public PaymentCallbackResult(boolean verified, String orderNo, String tradeNo,
                                 String channelOrderNo, String rawPayload) {
        this.verified = verified;
        this.orderNo = orderNo;
        this.tradeNo = tradeNo;
        this.channelOrderNo = channelOrderNo;
        this.rawPayload = rawPayload;
    }

    public boolean verified() { return verified; }
    public String orderNo() { return orderNo; }
    public String tradeNo() { return tradeNo; }
    public String channelOrderNo() { return channelOrderNo; }
    public String rawPayload() { return rawPayload; }
}
