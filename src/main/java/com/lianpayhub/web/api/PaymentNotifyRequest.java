package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class PaymentNotifyRequest {
    @NotBlank
    private String payload;

    public String payload() {
        return payload;
    }
}
