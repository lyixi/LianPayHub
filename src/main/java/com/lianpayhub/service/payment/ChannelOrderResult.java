package com.lianpayhub.service.payment;

import java.util.Collections;
import java.util.Map;

public class ChannelOrderResult {
    private final boolean supported;
    private final boolean success;
    private final String tradeNo;
    private final String status;
    private final String message;
    private final Map<String, Object> raw;

    public ChannelOrderResult(boolean supported, boolean success, String tradeNo, String status,
                              String message, Map<String, Object> raw) {
        this.supported = supported;
        this.success = success;
        this.tradeNo = tradeNo;
        this.status = status;
        this.message = message;
        this.raw = raw == null ? Collections.emptyMap() : raw;
    }

    public static ChannelOrderResult unsupported() {
        return new ChannelOrderResult(false, false, null, null, "渠道暂不支持查单", Collections.emptyMap());
    }

    public boolean isSupported() { return supported; }
    public boolean isSuccess() { return success; }
    public String getTradeNo() { return tradeNo; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Object> getRaw() { return raw; }
}
