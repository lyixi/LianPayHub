package com.lianpayhub.service.payment.provider;

public class AlipayConfig {
    private final String appId;
    private final String gatewayUrl;
    private final String merchantPrivateKey;
    private final String alipayPublicKey;
    private final String notifyUrl;
    private final String returnUrl;
    private final String charset;
    private final String signType;
    private final String defaultPayMode;

    public AlipayConfig(String appId, String gatewayUrl, String merchantPrivateKey, String alipayPublicKey,
                        String notifyUrl, String returnUrl, String charset, String signType, String defaultPayMode) {
        this.appId = appId;
        this.gatewayUrl = gatewayUrl;
        this.merchantPrivateKey = merchantPrivateKey;
        this.alipayPublicKey = alipayPublicKey;
        this.notifyUrl = notifyUrl;
        this.returnUrl = returnUrl;
        this.charset = charset;
        this.signType = signType;
        this.defaultPayMode = defaultPayMode;
    }

    public String appId() { return appId; }
    public String gatewayUrl() { return gatewayUrl; }
    public String merchantPrivateKey() { return merchantPrivateKey; }
    public String alipayPublicKey() { return alipayPublicKey; }
    public String notifyUrl() { return notifyUrl; }
    public String returnUrl() { return returnUrl; }
    public String charset() { return charset; }
    public String signType() { return signType; }
    public String defaultPayMode() { return defaultPayMode; }
}
