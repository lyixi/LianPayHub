package com.lianpayhub.service.notification.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.NotificationSendResult;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GenericHttpSmsGateway extends AbstractSmsGateway {

    private static final Logger log = LoggerFactory.getLogger(GenericHttpSmsGateway.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public GenericHttpSmsGateway(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(String providerCode) {
        return "aggregate".equals(providerCode)
                || "http".equals(providerCode)
                || "json-http".equals(providerCode)
                || "webhook".equals(providerCode)
                || "other".equals(providerCode);
    }

    @Override
    public NotificationSendResult send(NotificationChannelConfig config, SmsSendPayload payload) {
        JsonNode configJson = parseJson(config.getConfigJson());
        JsonNode credentialJson = parseJson(config.getCredentialJson());
        String endpoint = requireText(firstNonBlank(config.getEndpoint(), text(configJson, "endpoint"), text(configJson, "url")),
                "短信 HTTP endpoint 不能为空");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyAuthHeaders(headers, configJson, credentialJson);
        ObjectNode body = objectMapper.createObjectNode();
        put(body, "appId", payload.getAppId());
        put(body, "mobile", payload.getMobile());
        put(body, "phone", payload.getMobile());
        put(body, "content", payload.getContent());
        put(body, "templateCode", firstNonBlank(payload.getTemplateCode(), text(configJson, "templateCode")));
        put(body, "signName", firstNonBlank(config.getSenderName(), config.getSenderAddress(), text(configJson, "signName")));
        String paramsJson = firstNonBlank(payload.getParamsJson(), text(configJson, "templateParamJson"));
        if (paramsJson != null) {
            JsonNode paramsNode = parseJson(paramsJson);
            body.set("params", paramsNode);
            body.put("paramsJson", paramsJson);
        }
        if (configJson.has("extraBody") && configJson.get("extraBody").isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = configJson.get("extraBody").fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> item = fields.next();
                body.set(item.getKey(), item.getValue());
            }
        }
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<ObjectNode>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.SERVER_ERROR,
                        "短信 HTTP 发送失败: HTTP " + response.getStatusCodeValue());
            }
            String messageId = parseMessageId(response.getBody());
            log.info("http sms sent, provider={}, appId={}, mobile={}, messageId={}",
                    config.getProviderCode(), payload.getAppId(), maskMobile(payload.getMobile()), messageId);
            return new NotificationSendResult(true, NotificationChannelType.SMS, config.getProviderCode(), messageId,
                    "短信 HTTP 请求已提交");
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "短信 HTTP 发送异常: " + ex.getMessage());
        }
    }

    private void applyAuthHeaders(HttpHeaders headers, JsonNode configJson, JsonNode credentialJson) {
        if (configJson.has("headers") && configJson.get("headers").isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = configJson.get("headers").fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> item = fields.next();
                headers.set(item.getKey(), item.getValue().asText());
            }
        }
        String apiKey = text(credentialJson, "apiKey");
        if (apiKey != null) {
            headers.set(firstNonBlank(text(configJson, "apiKeyHeader"), "X-API-Key"), apiKey);
        }
        String token = firstNonBlank(text(credentialJson, "token"), text(credentialJson, "authorization"));
        if (token != null) {
            String headerName = firstNonBlank(text(configJson, "authHeaderName"), HttpHeaders.AUTHORIZATION);
            String prefix = firstNonBlank(text(configJson, "authHeaderPrefix"), "Bearer ");
            headers.set(headerName, prefix + token);
        }
    }

    private void put(ObjectNode node, String field, String value) {
        String safeValue = normalize(value);
        if (safeValue != null) {
            node.put(field, safeValue);
        }
    }

    private String parseMessageId(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "http-sms-" + UUID.randomUUID().toString();
        }
        try {
            JsonNode json = objectMapper.readTree(body);
            return firstNonBlank(
                    text(json, "messageId"),
                    text(json, "bizId"),
                    text(json, "requestId"),
                    text(json, "serialNo"),
                    "http-sms-" + UUID.randomUUID().toString());
        } catch (Exception ex) {
            return "http-sms-" + UUID.randomUUID().toString();
        }
    }
}
