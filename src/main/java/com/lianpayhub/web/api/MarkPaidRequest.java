package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class MarkPaidRequest {
    @NotBlank
    private String orderNo;
    @NotBlank
    private String tradeNo;

    public String orderNo() { return orderNo; }
    public String tradeNo() { return tradeNo; }
}
