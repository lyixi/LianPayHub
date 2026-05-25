package com.lianpayhub.service.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class ApiSignatureService {

    public String sign(String appId, String timestamp, String nonce, String method, String path, String key) {
        String payload = appId + "\n"
                + timestamp + "\n"
                + nonce + "\n"
                + method.toUpperCase() + "\n"
                + path;
        return hmacSha256(payload, key);
    }

    public boolean matches(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }

    private String hmacSha256(String payload, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item & 0xff));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("HmacSHA256 sign failed", ex);
        }
    }
}
