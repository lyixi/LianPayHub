package com.lianpayhub.service.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.*;
import com.lianpayhub.service.ai.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class AceDataProviderAdapter implements AiProviderAdapter {
    private final AceDataService service;
    private final ObjectMapper mapper;
    public AceDataProviderAdapter(AceDataService service, ObjectMapper mapper) { this.service = service; this.mapper = mapper; }
    @Override public String providerCode() { return "acedata"; }
    @Override public boolean supports(String code) { return "acedata".equalsIgnoreCase(code); }
    @Override public String createUserKey(AiProviderConfig config, AppAiProviderSetting setting, Long userId) {
        throw new BusinessException(ErrorCode.CONFLICT, "ACE Data 不支持自动创建用户 Key，请手动配置用户 Key");
    }
    @Override public AiChatResult chat(AiProviderConfig config, UserAiCredential credential, String model, String message, boolean stream, String imageUrl) {
        if (credential.getApiKey() == null || credential.getApiKey().trim().isEmpty()) throw new BusinessException(ErrorCode.FORBIDDEN, "用户未配置 ACE Data Key");
        if (stream) throw new BusinessException(ErrorCode.BAD_REQUEST, "ACE Data 聊天接口暂不支持流式返回");
        List<Object> content = new ArrayList<Object>();
        Map<String, Object> text = new LinkedHashMap<String, Object>();
        text.put("type", "text"); text.put("text", message); content.add(text);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Map<String, Object> image = new LinkedHashMap<String, Object>();
            image.put("type", "image_url"); image.put("image_url", Collections.singletonMap("url", imageUrl)); content.add(image);
        }
        Map<String, Object> msg = new LinkedHashMap<String, Object>(); msg.put("role", "user"); msg.put("content", content);
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("model", model); body.put("messages", Collections.singletonList(msg)); body.put("stream", false);
        JsonNode result = mapper.valueToTree(service.invoke(config, "openai-chat-completions", body, credential.getApiKey()));
        if (!result.path("choices").isArray() || result.path("choices").size() == 0) throw new BusinessException(ErrorCode.SERVER_ERROR, "ACE Data 未返回聊天结果");
        return new AiChatResult("acedata", model, false, result.path("choices").path(0).path("message").path("content").asText(""), result.path("id").asText(null));
    }
}
