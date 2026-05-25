package com.lianpayhub.web.admin;

import com.lianpayhub.domain.payment.PaymentChannelConfigStatus;
import javax.validation.constraints.NotNull;

public class ChangePaymentChannelConfigStatusRequest {
    @NotNull
    private PaymentChannelConfigStatus status;

    public PaymentChannelConfigStatus status() {
        return status;
    }
}
