package com.lianpayhub.service.notification.sms;

public class SmsSendPayload {

    private final String appId;
    private final String mobile;
    private final String content;
    private final String templateCode;
    private final String paramsJson;

    public SmsSendPayload(String appId, String mobile, String content, String templateCode, String paramsJson) {
        this.appId = appId;
        this.mobile = mobile;
        this.content = content;
        this.templateCode = templateCode;
        this.paramsJson = paramsJson;
    }

    public String getAppId() {
        return appId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getContent() {
        return content;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getParamsJson() {
        return paramsJson;
    }
}
