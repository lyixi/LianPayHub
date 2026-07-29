package com.lianpayhub.service.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.service.ai.AiChatResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MoacodeProviderAdapter implements AiProviderAdapter {
    private static final String DEFAULT_BASE_URL = "https://api.moacode.com/v1";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String providerCode() {
        return "moacode";
    }

    @Override
    public boolean supports(String providerCode) {
        return "moacode".equalsIgnoreCase(providerCode);
    }

    @Override
    public String createUserKey(AiProviderConfig providerConfig, AppAiProviderSetting appSetting, Long userId) {
        throw new BusinessException(ErrorCode.CONFLICT, "MoaCode 当前不支持平台自动创建用户 key");
    }

    @Override
    public AiChatResult chat(AiProviderConfig providerConfig, UserAiCredential credential, String model,
                             String message, boolean stream, String imageUrl) {
        String apiKey = credential.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户未分配 MoaCode key");
        }
        String safeModel = model == null || model.trim().isEmpty() ? defaultModel(providerConfig) : model.trim();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("model", safeModel);
            body.put("stream", false);
            body.put("messages", messages(message, imageUrl));

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl(providerConfig) + "/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<Map<String, Object>>(body, headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return new AiChatResult(providerCode(), safeModel, false, content, root.path("id").asText(null));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "MoaCode 调用失败: " + ex.getMessage());
        }
    }

    private java.util.List<Object> messages(String message, String imageUrl) {
        java.util.List<Object> messages = new ArrayList<Object>();
        Map<String, Object> msg = new LinkedHashMap<String, Object>();
        msg.put("role", "user");
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            java.util.List<Object> content = new ArrayList<Object>();
            Map<String, Object> text = new LinkedHashMap<String, Object>();
            text.put("type", "text");
            text.put("text", message == null ? "" : message);
            content.add(text);
            Map<String, Object> image = new LinkedHashMap<String, Object>();
            image.put("type", "image_url");
            image.put("image_url", java.util.Collections.singletonMap("url", imageUrl.trim()));
            content.add(image);
            msg.put("content", content);
        } else {
            msg.put("content", message == null ? "" : message);
        }
        messages.add(msg);
        return messages;
    }

    private String baseUrl(AiProviderConfig providerConfig) {
        String configured = providerConfig == null ? null : providerConfig.getBaseUrl();
        String value = configured == null || configured.trim().isEmpty() ? DEFAULT_BASE_URL : configured.trim();
        return value.replaceAll("/+$", "").replaceAll("(?i)/v1$", "") + "/v1";
    }

    private String defaultModel(AiProviderConfig providerConfig) {
        try {
            JsonNode root = objectMapper.readTree(providerConfig.getConfigJson() == null ? "{}" : providerConfig.getConfigJson());
            String model = root.path("defaultModel").asText(null);
            return model == null || model.trim().isEmpty() ? "gpt-5.3-codex" : model.trim();
        } catch (Exception ignored) {
            return "gpt-5.3-codex";
        }
    }
}
