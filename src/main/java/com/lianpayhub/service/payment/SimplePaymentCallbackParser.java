package com.lianpayhub.service.payment;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public final class SimplePaymentCallbackParser {

    private SimplePaymentCallbackParser() {
    }

    public static PaymentCallbackResult parse(String rawPayload) {
        Map<String, String> values = parseValues(rawPayload);
        String orderNo = first(values, "orderNo", "order_no", "out_trade_no");
        String tradeNo = first(values, "tradeNo", "trade_no", "transaction_id");
        String channelOrderNo = first(values, "channelOrderNo", "channel_order_no", "provider_order_no");
        boolean verified = Boolean.parseBoolean(first(values, "verified", "success", "paid"));
        if (!verified) {
            String status = first(values, "status", "trade_status", "result_code");
            verified = "SUCCESS".equalsIgnoreCase(status)
                    || "TRADE_SUCCESS".equalsIgnoreCase(status)
                    || "TRADE_FINISHED".equalsIgnoreCase(status);
        }
        return new PaymentCallbackResult(verified, orderNo, tradeNo, channelOrderNo, rawPayload);
    }

    private static Map<String, String> parseValues(String rawPayload) {
        Map<String, String> values = new HashMap<>();
        if (rawPayload == null) {
            return values;
        }
        String text = rawPayload.trim();
        if (text.startsWith("{") && text.endsWith("}")) {
            parseFlatJson(text, values);
        } else {
            parseForm(text, values);
        }
        return values;
    }

    // 只解析一层简单 JSON，真实支付回调接 SDK 后可直接替换 Provider 解析。
    private static void parseFlatJson(String text, Map<String, String> values) {
        String body = text.substring(1, text.length() - 1).trim();
        String[] parts = body.split(",");
        for (String part : parts) {
            int index = part.indexOf(':');
            if (index <= 0) {
                continue;
            }
            String key = clean(part.substring(0, index));
            String value = clean(part.substring(index + 1));
            if (!key.isEmpty()) {
                values.put(key, value);
            }
        }
    }

    private static void parseForm(String text, Map<String, String> values) {
        String[] parts = text.split("&");
        for (String part : parts) {
            int index = part.indexOf('=');
            if (index <= 0) {
                continue;
            }
            String key = decode(part.substring(0, index));
            String value = decode(part.substring(index + 1));
            values.put(key, value);
        }
    }

    private static String first(Map<String, String> values, String... keys) {
        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static String clean(String value) {
        String result = value == null ? "" : value.trim();
        if (result.length() >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return value;
        }
    }
}
