package com.lianpayhub.service.captcha;

public class CaptchaChallengeResult {
    private final String token;
    private final int expiresInSeconds;
    private final String debugCode;

    public CaptchaChallengeResult(String token, int expiresInSeconds, String debugCode) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
        this.debugCode = debugCode;
    }

    public String getToken() {
        return token;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public String getDebugCode() {
        return debugCode;
    }
}
