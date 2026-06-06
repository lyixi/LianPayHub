package com.lianpayhub.service.notification.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import java.io.IOException;

abstract class AbstractSmsGateway implements SmsGateway {

    protected final ObjectMapper objectMapper;

    protected AbstractSmsGateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected JsonNode parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node == null || !node.isObject() ? objectMapper.createObjectNode() : node;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "短信配置 JSON 格式不正确");
        }
    }

    protected String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return normalize(node.get(field).asText());
    }

    protected String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String safeValue = normalize(value);
            if (safeValue != null) {
                return safeValue;
            }
        }
        return null;
    }

    protected String requireText(String value, String message) {
        String safeValue = normalize(value);
        if (safeValue == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return safeValue;
    }

    protected String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "******";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
