package com.lianpayhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.service.security.ApiSignatureService;
import com.lianpayhub.service.security.AppSecretService;
import java.time.Instant;
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
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-signature-auth-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-api-signature-auth-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.api-auth-enabled=true",
        "lianpayhub.security.api-auth-mode=signature",
        "lianpayhub.security.api-signature-time-window-seconds=300",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class ApiSignatureAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiSignatureService apiSignatureService;

    @Autowired
    private AppSecretService appSecretService;

    @Test
    public void signatureModeShouldValidateTimestampSignatureAndNonce() throws Exception {
        String adminToken = adminToken();
        JsonNode app = createStandardApp(adminToken);
        String appSecret = text(app, "/data/appSecret");
        String signingKey = appSecretService.hashSecret(appSecret);

        JsonNode login = apiOk(requestJson(signedPost(
                "/api/auth/login",
                "auth-signature-app",
                signingKey,
                String.valueOf(Instant.now().getEpochSecond()),
                "nonce-ok-001",
                "{\"appId\":\"auth-signature-app\",\"mobile\":\"13800002222\"}"), 200));
        assertTrue(text(login, "/data/token").length() > 10);

        JsonNode duplicateNonce = requestJson(signedPost(
                "/api/auth/login",
                "auth-signature-app",
                signingKey,
                String.valueOf(Instant.now().getEpochSecond()),
                "nonce-ok-001",
                "{\"appId\":\"auth-signature-app\",\"mobile\":\"13800002222\"}"), 401);
        assertCode(duplicateNonce, 401);

        JsonNode expiredTimestamp = requestJson(signedPost(
                "/api/auth/login",
                "auth-signature-app",
                signingKey,
                String.valueOf(Instant.now().minusSeconds(600).getEpochSecond()),
                "nonce-expired-001",
                "{\"appId\":\"auth-signature-app\",\"mobile\":\"13800002222\"}"), 401);
        assertCode(expiredTimestamp, 401);

        JsonNode wrongSignature = requestJson(post("/api/auth/login")
                .header("X-App-Id", "auth-signature-app")
                .header("X-App-Timestamp", String.valueOf(Instant.now().getEpochSecond()))
                .header("X-App-Nonce", "nonce-wrong-001")
                .header("X-App-Signature", "bad-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-signature-app\",\"mobile\":\"13800002222\"}"), 401);
        assertCode(wrongSignature, 401);

        JsonNode validAfterWrongSignature = apiOk(requestJson(signedPost(
                "/api/auth/login",
                "auth-signature-app",
                signingKey,
                String.valueOf(Instant.now().getEpochSecond()),
                "nonce-wrong-001",
                "{\"appId\":\"auth-signature-app\",\"mobile\":\"13800002222\"}"), 200));
        assertEquals("auth-signature-app", text(validAfterWrongSignature, "/data/appId"));
    }

    private MockHttpServletRequestBuilder signedPost(String path, String appId, String signingKey,
                                                     String timestamp, String nonce, String body) {
        String signature = apiSignatureService.sign(appId, timestamp, nonce, "POST", path, signingKey);
        return post(path)
                .header("X-App-Id", appId)
                .header("X-App-Timestamp", timestamp)
                .header("X-App-Nonce", nonce)
                .header("X-App-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private String adminToken() throws Exception {
        JsonNode response = apiOk(requestJson(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"), 200));
        return text(response, "/data/token");
    }

    private JsonNode createStandardApp(String token) throws Exception {
        return apiOk(requestJson(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"auth-signature-app\",\"appName\":\"Signature App\",\"appType\":\"STANDARD\","
                        + "\"needMobileLogin\":true,\"needDeviceVip\":false}"), 200));
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
