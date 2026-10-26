package com.lianpayhub.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AiProviderConfig;
import java.io.IOException;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class AceDataService {
    public static final String BASE_URL = "https://api.acedata.cloud";
    private final ObjectMapper mapper;
    private final RestTemplate client;
    private final List<Map<String, Object>> operations;

    public AceDataService(ObjectMapper mapper) throws IOException {
        this.mapper = mapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(300000);
        this.client = new RestTemplate(factory);
        try (java.io.InputStream input = new ClassPathResource("ai/acedata-operations.json").getInputStream()) {
            operations = mapper.readValue(input, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    public List<Map<String, Object>> operations() { return operations; }

    public String platformKey(AiProviderConfig config) {
        try {
            JsonNode credential = mapper.readTree(config.getCredentialJson() == null ? "{}" : config.getCredentialJson());
            String key = credential.path("apiKey").asText("").trim();
            if (key.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置 ACE Data API Key");
            return key;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ACE Data 凭据格式错误");
        }
    }

    public Object invoke(AiProviderConfig config, String operationId, Map<String, Object> parameters, String apiKey) {
        if (!"acedata".equals(config.getProviderCode())) throw new BusinessException(ErrorCode.BAD_REQUEST, "该接口仅支持 ACE Data");
        if (!config.isEnabled()) throw new BusinessException(ErrorCode.CONFLICT, "ACE Data 平台已停用");
        Map<String, Object> operation = operations.stream().filter(o -> o.get("id").equals(operationId)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "不支持的 ACE Data 接口"));
        Map<String, Object> body = new LinkedHashMap<String, Object>(parameters == null ? Collections.emptyMap() : parameters);
        if (Boolean.TRUE.equals(body.get("stream"))) throw new BusinessException(ErrorCode.BAD_REQUEST, "当前工作台仅支持非流式请求，请设置 stream 为 false");
        String key = apiKey == null || apiKey.trim().isEmpty() ? platformKey(config) : apiKey.trim();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(key);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // The origin and path come from the bundled documentation, never from request parameters.
        String url = BASE_URL + operation.get("path");
        HttpMethod method = HttpMethod.valueOf((String) operation.get("method"));
        if (method == HttpMethod.GET) {
            org.springframework.web.util.UriComponentsBuilder query = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(url);
            for (Map.Entry<String, Object> entry : body.entrySet()) query.queryParam(entry.getKey(), entry.getValue());
            url = query.build().encode().toUriString();
        }
        try {
            ResponseEntity<byte[]> response = client.exchange(java.net.URI.create(url), method, new HttpEntity<Object>(method == HttpMethod.GET ? null : body, headers), byte[].class);
            byte[] bytes = response.getBody();
            if (bytes == null || bytes.length == 0) return Collections.emptyMap();
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType == null || contentType.isCompatibleWith(MediaType.APPLICATION_JSON) || contentType.getSubtype().endsWith("+json")) {
                return mapper.readValue(bytes, Object.class);
            }
            Map<String, Object> binary = new LinkedHashMap<String, Object>();
            binary.put("contentType", contentType.toString());
            binary.put("base64", Base64.getEncoder().encodeToString(bytes));
            return binary;
        } catch (HttpStatusCodeException ex) {
            // Avoid returning upstream bodies: they may contain request headers or credentials.
            throw new BusinessException(ErrorCode.SERVER_ERROR, "ACE Data 请求失败（HTTP " + ex.getRawStatusCode() + "），请检查密钥权限、余额和请求参数");
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "ACE Data 请求超时或响应无法解析，请使用任务查询接口确认状态，避免重复提交");
        }
    }
}
