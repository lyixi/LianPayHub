package com.lianpayhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-secret-auth-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-api-secret-auth-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.api-auth-enabled=true",
        "lianpayhub.security.api-auth-mode=secret",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456",
        "lianpayhub.payment.dev-tools-enabled=true"
})
@AutoConfigureMockMvc
public class ApiSecretAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void secretModeShouldProtectStandardAppsAndAllowDeviceOnlyPublicFlow() throws Exception {
        String adminToken = adminToken();
        JsonNode standardApp = createApp(adminToken, "auth-standard-app", "STANDARD", true, false);
        JsonNode deviceApp = createApp(adminToken, "auth-device-app", "DEVICE_ONLY", false, true);
        String standardSecret = text(standardApp, "/data/appSecret");
        Long devicePackageId = createPackage(adminToken, "auth-device-app");

        JsonNode missingSecret = requestJson(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-standard-app\",\"mobile\":\"13800001111\"}"), 401);
        assertCode(missingSecret, 401);

        JsonNode wrongSecret = requestJson(post("/api/auth/login")
                .header("X-App-Id", "auth-standard-app")
                .header("X-App-Secret", "wrong-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-standard-app\",\"mobile\":\"13800001111\"}"), 401);
        assertCode(wrongSecret, 401);

        JsonNode login = apiOk(requestJson(post("/api/auth/login")
                .header("X-App-Id", "auth-standard-app")
                .header("X-App-Secret", standardSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-standard-app\",\"mobile\":\"13800001111\"}"), 200));
        assertTrue(text(login, "/data/token").length() > 10);

        JsonNode standardDeviceWithoutSecret = requestJson(post("/api/device/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-standard-app\",\"deviceCode\":\"std-device-001\"}"), 401);
        assertCode(standardDeviceWithoutSecret, 401);

        JsonNode device = apiOk(requestJson(post("/api/device/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-device-app\",\"deviceCode\":\"device-only-001\",\"deviceName\":\"Device Only\"}"), 200));
        Long deviceId = longValue(device, "/data/id");

        JsonNode memberStatus = apiOk(requestJson(get("/api/member/status")
                .param("appId", "auth-device-app")
                .param("deviceCode", "device-only-001"), 200));
        assertFalse(memberStatus.path("data").path("active").asBoolean());

        JsonNode order = apiOk(requestJson(post("/api/payment/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-device-app\",\"deviceId\":" + deviceId
                        + ",\"packageId\":" + devicePackageId + ",\"payChannel\":\"AGGREGATE\"}"), 200));
        assertTrue(text(order, "/data/orderNo").startsWith("LFP"));

        JsonNode notifyBypass = requestJson(post("/api/payment/notify/AGGREGATE")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payload\":\"verified=true&orderNo=NOT_EXIST&tradeNo=NOPE&status=SUCCESS\"}"), 200);
        assertCode(notifyBypass, 404);

        assertTrue(text(deviceApp, "/data/appSecret").length() > 10);
    }

    private String adminToken() throws Exception {
        JsonNode response = apiOk(requestJson(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"), 200));
        return text(response, "/data/token");
    }

    private JsonNode createApp(String token, String appId, String appType, boolean needMobileLogin,
                               boolean needDeviceVip) throws Exception {
        return apiOk(requestJson(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"appName\":\"" + appId + "\",\"appType\":\"" + appType
                        + "\",\"needMobileLogin\":" + needMobileLogin + ",\"needDeviceVip\":" + needDeviceVip + "}"), 200));
    }

    private Long createPackage(String token, String appId) throws Exception {
        JsonNode response = apiOk(requestJson(post("/admin/packages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"packageName\":\"Auth Device Month\",\"packageType\":\"MEMBERSHIP\","
                        + "\"priceCents\":990,\"durationDays\":30,\"benefitsText\":\"auth test\"}"), 200));
        return longValue(response, "/data/id");
    }

    private JsonNode requestJson(MockHttpServletRequestBuilder builder, int expectedHttpStatus) throws Exception {
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().is(expectedHttpStatus))
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

    private Long longValue(JsonNode response, String pointer) {
        JsonNode node = response.at(pointer);
        assertFalse(node.isMissingNode(), "missing json path: " + pointer + " in " + response);
        return node.asLong();
    }
}
