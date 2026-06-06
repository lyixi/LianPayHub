package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class SendSmsMessageRequest {
    private Long configId;
    private String appId;
    @NotBlank
    private String mobile;
    private String templateCode;
    private String paramsJson;

    public Long configId() { return configId; }
    public String appId() { return appId; }
    public String mobile() { return mobile; }
    public String templateCode() { return templateCode; }
    public String paramsJson() { return paramsJson; }
}
