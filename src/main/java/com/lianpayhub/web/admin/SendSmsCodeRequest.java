package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class SendSmsCodeRequest {
    private Long configId;
    private String appId;
    @NotBlank
    private String mobile;
    @NotBlank
    private String code;
    private Boolean realSend;

    public Long configId() { return configId; }
    public String appId() { return appId; }
    public String mobile() { return mobile; }
    public String code() { return code; }
    public Boolean realSend() { return realSend; }
}
