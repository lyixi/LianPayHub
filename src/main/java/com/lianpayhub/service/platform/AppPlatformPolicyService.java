package com.lianpayhub.service.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.AppPlatformPolicyRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppPlatformPolicyService {

    private final AppPlatformPolicyRepository repository;
    private final AppInfoRepository appInfoRepository;
    private final ObjectMapper objectMapper;

    public AppPlatformPolicyService(AppPlatformPolicyRepository repository,
                                    AppInfoRepository appInfoRepository,
                                    ObjectMapper objectMapper) {
        this.repository = repository;
        this.appInfoRepository = appInfoRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AppPlatformPolicy> list(String appId) {
        String safeAppId = requireAppId(appId);
        return repository.findByAppIdOrderByCategoryAsc(safeAppId);
    }

    @Transactional(readOnly = true)
    public Optional<AppPlatformPolicy> find(String appId, PlatformConfigCategory category) {
        if (appId == null || appId.trim().isEmpty() || category == null) {
            return Optional.empty();
        }
        return repository.findByAppIdAndCategory(appId.trim(), category);
    }

    @Transactional(readOnly = true)
    public AppPlatformPolicy requireEnabled(String appId, PlatformConfigCategory category) {
        AppPlatformPolicy policy = find(appId, category)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 未配置该平台策略"));
        if (!policy.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP 平台策略已停用: " + category);
        }
        return policy;
    }

    @Transactional
    public AppPlatformPolicy upsert(String appId, PlatformConfigCategory category, boolean enabled,
                                    String providerCode, String configJson, String credentialJson,
                                    String policyJson) {
        String safeAppId = requireAppId(appId);
        PlatformConfigCategory safeCategory = requireCategory(category);
        validateJson(configJson, "configJson");
        validateJson(credentialJson, "credentialJson");
        validateJson(policyJson, "policyJson");
        AppPlatformPolicy current = repository.findByAppIdAndCategory(safeAppId, safeCategory).orElse(null);
        if (current == null) {
            return repository.save(new AppPlatformPolicy(safeAppId, safeCategory, enabled,
                    normalize(providerCode), normalize(configJson), normalize(credentialJson), normalize(policyJson)));
        }
        current.update(enabled, normalize(providerCode), normalize(configJson), normalize(credentialJson),
                normalize(policyJson));
        return repository.save(current);
    }

    public JsonNode policyJson(AppPlatformPolicy policy) {
        return parseObject(policy == null ? null : policy.getPolicyJson());
    }

    public JsonNode configJson(AppPlatformPolicy policy) {
        return parseObject(policy == null ? null : policy.getConfigJson());
    }

    public String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return normalize(node.get(field).asText());
    }

    public int intValue(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asInt(defaultValue);
    }

    public boolean booleanValue(JsonNode node, String field, boolean defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asBoolean(defaultValue);
    }

    private String requireAppId(String appId) {
        String safeAppId = normalize(appId);
        if (safeAppId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "appId 不能为空");
        }
        if (!appInfoRepository.existsByAppId(safeAppId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        }
        return safeAppId;
    }

    private PlatformConfigCategory requireCategory(PlatformConfigCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "category 不能为空");
        }
        return category;
    }

    private void validateJson(String json, String fieldName) {
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        parseObject(json, fieldName);
    }

    private JsonNode parseObject(String json) {
        return parseObject(json, "JSON");
    }

    private JsonNode parseObject(String json, String fieldName) {
        if (json == null || json.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.isObject()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 必须是 JSON 对象");
            }
            return node;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式不正确");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
