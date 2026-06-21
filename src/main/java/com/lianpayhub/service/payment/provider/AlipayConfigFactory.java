package com.lianpayhub.service.payment.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AlipayConfigFactory {
    private static final String SANDBOX_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String PROD_GATEWAY = "https://openapi.alipay.com/gateway.do";

    private final ObjectMapper objectMapper;

    public AlipayConfigFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AlipayConfig create(PaymentChannelConfig config, String overrideReturnUrl) {
        if (config == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝支付需要先配置支付渠道");
        }
        Map<String, Object> normal = parse(config.getConfigJson());
        Map<String, Object> credential = parse(config.getCredentialJson());
        boolean sandbox = booleanValue(normal.get("sandbox"));
        String prefix = sandbox ? "sandbox" : "prod";
        String gatewayUrl = firstText(normal.get(prefix + "GatewayUrl"), normal.get("gatewayUrl"));
        if (gatewayUrl == null) {
            gatewayUrl = sandbox ? SANDBOX_GATEWAY : PROD_GATEWAY;
        }
        String appId = firstText(normal.get(prefix + "AppId"), config.getChannelAppId());
        String notifyUrl = firstText(normal.get(prefix + "NotifyUrl"), config.getNotifyUrl());
        String privateKey = firstText(credential.get(prefix + "MerchantPrivateKey"), credential.get("merchantPrivateKey"), credential.get("privateKey"), credential.get("appPrivateKey"));
        String alipayPublicKey = firstText(credential.get(prefix + "AlipayPublicKey"), credential.get("alipayPublicKey"), credential.get("publicKey"));
        String returnUrl = firstText(overrideReturnUrl, normal.get(prefix + "ReturnUrl"), normal.get("returnUrl"));
        return new AlipayConfig(
                requireText(appId, "支付宝 appId 不能为空"),
                gatewayUrl,
                requireText(privateKey, "支付宝应用私钥不能为空"),
                requireText(alipayPublicKey, "支付宝公钥不能为空"),
                notifyUrl,
                returnUrl,
                firstText(normal.get(prefix + "Charset"), normal.get("charset"), "UTF-8"),
                firstText(normal.get(prefix + "SignType"), normal.get("signType"), "RSA2"),
                firstText(normal.get(prefix + "DefaultPayMode"), normal.get("defaultPayMode"), "PAGE")
        );
    }

    private Map<String, Object> parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝配置 JSON 格式错误");
        }
    }

    private String requireText(String value, String message) {
        String text = text(value);
        if (text == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return text;
    }

    private String firstText(Object... values) {
        if (values == null) return null;
        for (Object value : values) {
            String text = text(value);
            if (text != null) return text;
        }
        return null;
    }

    private String text(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private boolean booleanValue(Object value) {
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }
}
