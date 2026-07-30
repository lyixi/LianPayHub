package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;

    public String refreshToken() { return refreshToken; }
}
