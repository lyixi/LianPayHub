package com.lianpayhub.web.api;

import com.lianpayhub.domain.payment.PayChannel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreatePublicPurchaseOrderRequest {
    @NotBlank
    private String pageSlug;
    private Long productId;
    private Long planId;
    private String productCode;
    private String productType;
    private String planCode;
    private Long userId;
    private Long deviceId;
    private String deviceCode;
    @NotNull
    private PayChannel payChannel;

    public String pageSlug() { return pageSlug; }
    public Long productId() { return productId; }
    public Long planId() { return planId; }
    public String productCode() { return productCode; }
    public String productType() { return productType; }
    public String planCode() { return planCode; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public String deviceCode() { return deviceCode; }
    public PayChannel payChannel() { return payChannel; }
}
