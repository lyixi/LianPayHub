package com.lianpayhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.packageinfo.PackageType;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.repository.SearchPlatformConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-platform-policy-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.show-sql=false",
        "spring.main.banner-mode=off",
        "spring.main.log-startup-info=false",
        "debug=false",
        "logging.level.root=WARN",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate=WARN",
        "logging.level.com.lianpayhub=INFO",
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-platform-policy-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.sms-debug-return-code=true",
        "lianpayhub.security.api-auth-enabled=false",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class AppPlatformPolicyIntegrationTest {

    private static final String APP_ID = "policy-standard-app";
    private static final String DEVICE_APP_ID = "policy-device-app";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PackageInfoRepository packageInfoRepository;

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private SearchPlatformConfigRepository searchPlatformConfigRepository;

    @Test
    public void appPoliciesShouldControlRuntimeFeatures() throws Exception {
        String adminToken = adminToken();
        createApp(adminToken, APP_ID, "STANDARD", true, false);
        createApp(adminToken, DEVICE_APP_ID, "DEVICE_ONLY", false, true);

        upsertPolicy(adminToken, APP_ID, "SMS", false, "local", "{}", "{}", "{}");
        assertCode(postJson("/api/auth/send-code",
                "{\"appId\":\"" + APP_ID + "\",\"mobile\":\"13800007777\"}"), 409);

        upsertPolicy(adminToken, APP_ID, "CAPTCHA", true, "local", "{}", "{}", "{\"ttlSeconds\":60,\"length\":4,\"maxAttempts\":2,\"debugReturnCode\":true}");
        JsonNode challenge = apiOk(postJson("/api/captcha/challenge",
                "{\"appId\":\"" + APP_ID + "\",\"purpose\":\"login\"}"));
        String token = text(challenge, "/data/token");
        String code = text(challenge, "/data/debugCode");
        assertEquals(4, code.length());
        apiOk(postJson("/api/captcha/verify",
                "{\"appId\":\"" + APP_ID + "\",\"purpose\":\"login\",\"token\":\"" + token + "\",\"code\":\"" + code + "\"}"));
        assertCode(postJson("/api/captcha/verify",
                "{\"appId\":\"" + APP_ID + "\",\"purpose\":\"login\",\"token\":\"" + token + "\",\"code\":\"" + code + "\"}"), 400);

        apiOk(postJson(post("/admin/payment-configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"__PLATFORM__\",\"payChannel\":\"WECHAT\",\"providerCode\":\"wechat\",\"merchantId\":\"mch-001\",\"channelAppId\":\"wx-app-001\"}")));
        PackageInfo devicePackage = packageInfoRepository.save(new PackageInfo(
                DEVICE_APP_ID, "设备会员", PackageType.MEMBERSHIP, 9900, 30, "device vip"));
        DeviceInfo device = deviceInfoRepository.save(new DeviceInfo(
                DEVICE_APP_ID, "device-policy-001", "policy-device", "ios", "fp-policy"));
        JsonNode order = apiOk(postJson("/api/payment/create-order",
                "{\"appId\":\"" + DEVICE_APP_ID + "\",\"deviceId\":" + device.getId()
                        + ",\"packageId\":" + devicePackage.getId() + ",\"payChannel\":\"WECHAT\"}"));
        assertEquals(PayChannel.WECHAT, paymentOrderRepository.findByOrderNo(text(order, "/data/orderNo")).get().getPayChannel());

        apiOk(postJson(post("/admin/search-platforms")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providerCode\":\"bocha\",\"displayName\":\"博查搜索\",\"baseUrl\":\"https://api.bocha.com\",\"configJson\":\"{}\",\"credentialJson\":\"{}\"}")));
        JsonNode searchPlatforms = apiOk(getJson(get("/admin/search-platforms")
                .header("Authorization", "Bearer " + adminToken)));
        assertTrue(searchPlatforms.path("data").isArray());
        assertEquals("bocha", text(searchPlatforms, "/data/0/providerCode"));
    }

    private String adminToken() throws Exception {
        JsonNode response = apiOk(postJson("/admin/auth/login",
                "{\"username\":\"admin\",\"password\":\"admin123456\"}"));
        return text(response, "/data/token");
    }

    private void createApp(String token, String appId, String appType, boolean needMobileLogin,
                           boolean needDeviceVip) throws Exception {
        apiOk(postJson(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"appName\":\"" + appId + "\",\"appType\":\"" + appType
                        + "\",\"needMobileLogin\":" + needMobileLogin + ",\"needDeviceVip\":" + needDeviceVip + "}")));
    }

    private void upsertPolicy(String token, String appId, String category, boolean enabled,
                              String providerCode, String configJson, String credentialJson,
                              String policyJson) throws Exception {
        apiOk(postJson(post("/admin/app-platform-policies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"category\":\"" + category + "\",\"enabled\":" + enabled
                        + ",\"providerCode\":\"" + providerCode + "\",\"configJson\":" + quote(configJson)
                        + ",\"credentialJson\":" + quote(credentialJson) + ",\"policyJson\":" + quote(policyJson) + "}")));
    }

    private JsonNode postJson(String path, String json) throws Exception {
        return postJson(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private JsonNode postJson(MockHttpServletRequestBuilder builder) throws Exception {
        return requestJson(builder);
    }

    private JsonNode getJson(MockHttpServletRequestBuilder builder) throws Exception {
        return requestJson(builder);
    }

    private JsonNode requestJson(MockHttpServletRequestBuilder builder) throws Exception {
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode apiOk(JsonNode response) {
        assertCode(response, 0);
        return response;
    }

    private void assertCode(JsonNode response, int code) {
        assertEquals(code, response.path("code").asInt(), response.toString());
    }

    private String text(JsonNode response, String pointer) {
        JsonNode node = response.at(pointer);
        assertFalse(node.isMissingNode(), "missing json path: " + pointer + " in " + response);
        return node.asText();
    }

    private String quote(String value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
