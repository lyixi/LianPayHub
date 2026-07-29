package com.lianpayhub.service.ai;

public class AiChatResult {
    private final String providerCode;
    private final String model;
    private final boolean stream;
    private final String content;
    private final String requestId;

    public AiChatResult(String providerCode, String model, boolean stream, String content) {
        this(providerCode, model, stream, content, null);
    }

    public AiChatResult(String providerCode, String model, boolean stream, String content, String requestId) {
        this.providerCode = providerCode;
        this.model = model;
        this.stream = stream;
        this.content = content;
        this.requestId = requestId;
    }

    public String getProviderCode() { return providerCode; }
    public String getModel() { return model; }
    public boolean isStream() { return stream; }
    public String getContent() { return content; }
    public String getRequestId() { return requestId; }
}
