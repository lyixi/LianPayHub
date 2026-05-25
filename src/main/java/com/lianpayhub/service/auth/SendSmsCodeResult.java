package com.lianpayhub.service.auth;

public class SendSmsCodeResult {

    private final boolean sent;
    private final int expireSeconds;
    private final String debugCode;

    public SendSmsCodeResult(boolean sent, int expireSeconds, String debugCode) {
        this.sent = sent;
        this.expireSeconds = expireSeconds;
        this.debugCode = debugCode;
    }

    public boolean isSent() {
        return sent;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public String getDebugCode() {
        return debugCode;
    }
}
