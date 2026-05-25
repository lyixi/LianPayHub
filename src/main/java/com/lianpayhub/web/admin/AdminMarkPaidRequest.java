package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class AdminMarkPaidRequest {
    @NotBlank
    private String tradeNo;

    public String tradeNo() {
        return tradeNo;
    }
}
