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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-sms-code-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-sms-code-test",
        "lianpayhub.security.sms-code-required=true",
        "lianpayhub.security.sms-code-cooldown-seconds=0",
        "lianpayhub.security.sms-code-max-attempts=2",
        "lianpayhub.security.sms-debug-return-code=true",
        "lianpayhub.security.api-auth-enabled=false",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class SmsCodeIntegrationTest {

    private static final String STANDARD_APP = "sms-standard-app";
    private static final String DEVICE_APP = "sms-device-app";
    private static final String MOBILE = "13800005555";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void mobileLoginShouldVerifyAndConsumeSmsCode() throws Exception {
        String adminToken = adminToken();
        createApp(adminToken, STANDARD_APP, "STANDARD", true, false);
        createApp(adminToken, DEVICE_APP, "DEVICE_ONLY", false, true);

        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\"}", 400);

        JsonNode sendCode = postOk("/api/auth/send-code",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\"}");
        assertTrue(sendCode.path("data").path("sent").asBoolean());
        assertEquals(300, sendCode.path("data").path("expireSeconds").asInt());
        String code = text(sendCode, "/data/debugCode");
        assertEquals(6, code.length());

        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"000000\"}", 400);

        JsonNode login = postOk("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"" + code + "\"}");
        assertTrue(text(login, "/data/token").length() > 20);
        assertEquals(MOBILE, text(login, "/data/mobile"));

        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"" + code + "\"}", 400);

        JsonNode secondCode = postOk("/api/auth/send-code",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\"}");
        String lockedCode = text(secondCode, "/data/debugCode");
        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"111111\"}", 400);
        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"222222\"}", 400);
        postCode("/api/auth/login",
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"" + lockedCode + "\"}", 400);

        postCode("/api/auth/send-code",
                "{\"appId\":\"" + DEVICE_APP + "\",\"mobile\":\"13800006666\"}", 409);
        postCode("/api/auth/login",
                "{\"appId\":\"" + DEVICE_APP + "\",\"mobile\":\"13800006666\",\"code\":\"123456\"}", 409);
    }

    private String adminToken() throws Exception {
        JsonNode response = postOk("/admin/auth/login",
                "{\"username\":\"admin\",\"password\":\"admin123456\"}");
        return text(response, "/data/token");
    }

    private void createApp(String token, String appId, String appType, boolean needMobileLogin,
                           boolean needDeviceVip) throws Exception {
        postOk(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"appName\":\"" + appId + "\",\"appType\":\"" + appType
                        + "\",\"needMobileLogin\":" + needMobileLogin + ",\"needDeviceVip\":" + needDeviceVip + "}"));
    }

    private JsonNode postOk(String path, String json) throws Exception {
        return apiOk(requestJson(post(path).contentType(MediaType.APPLICATION_JSON).content(json), 200));
    }

    private JsonNode postOk(MockHttpServletRequestBuilder builder) throws Exception {
        return apiOk(requestJson(builder, 200));
    }

    private JsonNode postCode(String path, String json, int code) throws Exception {
        JsonNode response = requestJson(post(path).contentType(MediaType.APPLICATION_JSON).content(json), 200);
        assertCode(response, code);
        return response;
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
}
