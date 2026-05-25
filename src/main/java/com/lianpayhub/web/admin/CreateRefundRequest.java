package com.lianpayhub.web.admin;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CreateRefundRequest {
    @NotNull
    private Long orderId;
    @NotNull
    @Min(1)
    private Integer amountCents;
    private String reason;

    public Long orderId() { return orderId; }
    public Integer amountCents() { return amountCents; }
    public String reason() { return reason; }
}
