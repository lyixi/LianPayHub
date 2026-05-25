package com.lianpayhub.service.auth;

public interface SmsSender {
    void send(String appId, String mobile, String code, int expireMinutes);
}
