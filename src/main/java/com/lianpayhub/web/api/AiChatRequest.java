package com.lianpayhub.web.api;

import javax.validation.constraints.NotBlank;

public class AiChatRequest {
    @NotBlank
    private String appId;
    private String providerCode;
    @NotBlank
    private String model;
    @NotBlank
    private String message;
    private boolean stream;
    private String imageUrl;

    public String appId() { return appId; }
    public String providerCode() { return providerCode; }
    public String model() { return model; }
    public String message() { return message; }
    public boolean stream() { return stream; }
    public String imageUrl() { return imageUrl; }
}
