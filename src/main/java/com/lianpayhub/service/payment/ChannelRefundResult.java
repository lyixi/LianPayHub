package com.lianpayhub.service.payment;

import java.util.Collections;
import java.util.Map;

public class ChannelRefundResult {
    private final boolean supported;
    private final boolean success;
    private final String channelRefundNo;
    private final String message;
    private final Map<String, Object> raw;

    public ChannelRefundResult(boolean supported, boolean success, String channelRefundNo,
                               String message, Map<String, Object> raw) {
        this.supported = supported;
        this.success = success;
        this.channelRefundNo = channelRefundNo;
        this.message = message;
        this.raw = raw == null ? Collections.emptyMap() : raw;
    }

    public static ChannelRefundResult unsupported() {
        return new ChannelRefundResult(false, false, null, "渠道暂不支持退款", Collections.emptyMap());
    }

    public boolean isSupported() { return supported; }
    public boolean isSuccess() { return success; }
    public String getChannelRefundNo() { return channelRefundNo; }
    public String getMessage() { return message; }
    public Map<String, Object> getRaw() { return raw; }
}
