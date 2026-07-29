package com.lianpayhub.service.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.service.ai.AiChatResult;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Api2dProviderAdapter implements AiProviderAdapter {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String providerCode() { return "api2d"; }

    @Override
    public boolean supports(String providerCode) { return "api2d".equalsIgnoreCase(providerCode); }

    @Override
    public String createUserKey(AiProviderConfig providerConfig, AppAiProviderSetting appSetting, Long userId) {
        String adminToken = credential(providerConfig, "adminApiKey");
        String baseUrl = providerConfig.getBaseUrl() == null || providerConfig.getBaseUrl().trim().isEmpty() ? "https://oa.api2d.net" : providerConfig.getBaseUrl();
        String groupId = appSetting.getKeyGroupId();
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "APP 尚未配置 API2D key 分组 ID");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("type_id", Integer.valueOf(groupId));
            body.put("n", 1);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/custom_key/save", HttpMethod.POST, new HttpEntity<Map<String, Object>>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode key = root.path("data").path("custom_key_array").path(0).path("key");
            if (key.isMissingNode() || key.asText().trim().isEmpty()) throw new BusinessException(ErrorCode.SERVER_ERROR, "API2D 未返回新 key");
            return key.asText().trim();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "API2D 创建 key 失败: " + ex.getMessage());
        }
    }

    @Override
    public AiChatResult chat(AiProviderConfig providerConfig, UserAiCredential credential, String model, String message, boolean stream, String imageUrl) {
        String apiKey = credential.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户未分配 API2D key");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("model", model);
            body.put("stream", false);
            java.util.List<Object> messages = new java.util.ArrayList<Object>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Map<String, Object> msg = new LinkedHashMap<String, Object>();
                msg.put("role", "user");
                java.util.List<Object> content = new java.util.ArrayList<Object>();
                Map<String, Object> text = new LinkedHashMap<String, Object>();
                text.put("type", "text"); text.put("text", message); content.add(text);
                Map<String, Object> image = new LinkedHashMap<String, Object>();
                image.put("type", "image_url"); image.put("image_url", java.util.Collections.singletonMap("url", imageUrl)); content.add(image);
                msg.put("content", content); messages.add(msg);
            } else {
                Map<String, Object> msg = new LinkedHashMap<String, Object>(); msg.put("role", "user"); msg.put("content", message); messages.add(msg);
            }
            body.put("messages", messages);
            ResponseEntity<String> response = restTemplate.exchange(providerConfig.getBaseUrl() + "/chat/completions", HttpMethod.POST, new HttpEntity<Map<String, Object>>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return new AiChatResult("api2d", model, stream, content, root.path("id").asText(null));
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "API2D 调用失败: " + ex.getMessage());
        }
    }

    private String credential(AiProviderConfig config, String field) {
        try {
            JsonNode root = objectMapper.readTree(config.getCredentialJson() == null ? "{}" : config.getCredentialJson());
            String value = root.path(field).asText(null);
            if (value == null || value.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "AI平台缺少凭据字段: " + field);
            }
            return value.trim();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI平台凭据 JSON 格式错误");
        }
    }
}
