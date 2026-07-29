package com.lianpayhub.service.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.service.ai.AiChatResult;
import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeepSeekProviderAdapter implements AiProviderAdapter {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override public String providerCode() { return "deepseek"; }
    @Override public boolean supports(String providerCode) { return "deepseek".equalsIgnoreCase(providerCode); }
    @Override public String createUserKey(AiProviderConfig providerConfig, AppAiProviderSetting appSetting, Long userId) { throw new BusinessException(ErrorCode.CONFLICT, "DeepSeek 当前不支持平台自动创建用户 key"); }
    @Override
    public AiChatResult chat(AiProviderConfig providerConfig, UserAiCredential credential, String model, String message, boolean stream, String imageUrl) {
        String apiKey = credential.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "用户未分配 DeepSeek key");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("model", model);
            body.put("stream", false);
            java.util.List<Object> messages = new ArrayList<Object>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Map<String, Object> msg = new LinkedHashMap<String, Object>();
                msg.put("role", "user");
                java.util.List<Object> content = new ArrayList<Object>();
                Map<String, Object> text = new LinkedHashMap<String, Object>();
                text.put("type", "text"); text.put("text", message); content.add(text);
                Map<String, Object> image = new LinkedHashMap<String, Object>();
                image.put("type", "image_url"); image.put("image_url", Collections.singletonMap("url", imageUrl)); content.add(image);
                msg.put("content", content); messages.add(msg);
            } else {
                Map<String, Object> msg = new LinkedHashMap<String, Object>(); msg.put("role", "user"); msg.put("content", message); messages.add(msg);
            }
            body.put("messages", messages);
            ResponseEntity<String> response = restTemplate.exchange(providerConfig.getBaseUrl() + "/chat/completions", HttpMethod.POST, new HttpEntity<Map<String, Object>>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return new AiChatResult("deepseek", model, stream, content, root.path("id").asText(null));
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "DeepSeek 调用失败: " + ex.getMessage());
        }
    }
}
