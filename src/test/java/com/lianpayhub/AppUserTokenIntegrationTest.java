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
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-user-token-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-app-user-token-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.api-auth-enabled=true",
        "lianpayhub.security.api-auth-mode=secret",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class AppUserTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void standardAppUserApisShouldRequireMatchingUserToken() throws Exception {
        String adminToken = adminToken();
        JsonNode appOne = createApp(adminToken, "user-token-app-one");
        JsonNode appTwo = createApp(adminToken, "user-token-app-two");
        String appOneSecret = text(appOne, "/data/appSecret");
        String appTwoSecret = text(appTwo, "/data/appSecret");
        Long packageId = createPackage(adminToken, "user-token-app-one");

        JsonNode firstLogin = appLogin("user-token-app-one", appOneSecret, "13800003333");
        Long firstUserId = longValue(firstLogin, "/data/userId");
        String firstUserToken = text(firstLogin, "/data/token");

        JsonNode secondLogin = appLogin("user-token-app-one", appOneSecret, "13800004444");
        Long secondUserId = longValue(secondLogin, "/data/userId");
        String secondUserToken = text(secondLogin, "/data/token");

        JsonNode otherAppLogin = appLogin("user-token-app-two", appTwoSecret, "13800003333");
        assertEquals(firstUserId.longValue(), longValue(otherAppLogin, "/data/userId").longValue());
        String otherAppToken = text(otherAppLogin, "/data/token");

        postCode(createOrderRequest(firstUserId, packageId)
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret), 403);

        postCode(createOrderRequest(firstUserId, packageId)
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + secondUserToken), 403);

        postCode(createOrderRequest(firstUserId, packageId)
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + otherAppToken), 403);

        JsonNode order = apiOk(requestJson(createOrderRequest(firstUserId, packageId)
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + firstUserToken), 200));
        assertTrue(text(order, "/data/orderNo").startsWith("LFP"));

        JsonNode status = apiOk(requestJson(get("/api/member/status")
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + firstUserToken)
                .param("appId", "user-token-app-one")
                .param("userId", String.valueOf(firstUserId)), 200));
        assertFalse(status.path("data").path("active").asBoolean());

        JsonNode wrongMemberUser = requestJson(get("/api/member/status")
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + firstUserToken)
                .param("appId", "user-token-app-one")
                .param("userId", String.valueOf(secondUserId)), 200);
        assertCode(wrongMemberUser, 403);

        JsonNode wrongMemberApp = requestJson(get("/api/member/status")
                .header("X-App-Id", "user-token-app-one")
                .header("X-App-Secret", appOneSecret)
                .header("Authorization", "Bearer " + otherAppToken)
                .param("appId", "user-token-app-one")
                .param("userId", String.valueOf(firstUserId)), 200);
        assertCode(wrongMemberApp, 403);
    }

    private MockHttpServletRequestBuilder createOrderRequest(Long userId, Long packageId) {
        return post("/api/payment/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"user-token-app-one\",\"userId\":" + userId
                        + ",\"packageId\":" + packageId + ",\"payChannel\":\"WECHAT\"}");
    }

    private String adminToken() throws Exception {
        JsonNode response = apiOk(requestJson(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"), 200));
        return text(response, "/data/token");
    }

    private JsonNode createApp(String token, String appId) throws Exception {
        return apiOk(requestJson(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"appName\":\"" + appId + "\",\"appType\":\"STANDARD\","
                        + "\"needMobileLogin\":true,\"needDeviceVip\":false}"), 200));
    }

    private Long createPackage(String token, String appId) throws Exception {
        JsonNode response = apiOk(requestJson(post("/admin/packages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"packageName\":\"User Token Month\",\"packageType\":\"MEMBERSHIP\","
                        + "\"priceCents\":1299,\"durationDays\":30,\"benefitsText\":\"user token\"}"), 200));
        return longValue(response, "/data/id");
    }

    private JsonNode appLogin(String appId, String appSecret, String mobile) throws Exception {
        return apiOk(requestJson(post("/api/auth/login")
                .header("X-App-Id", appId)
                .header("X-App-Secret", appSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"mobile\":\"" + mobile + "\"}"), 200));
    }

    private JsonNode postCode(MockHttpServletRequestBuilder builder, int code) throws Exception {
        JsonNode response = requestJson(builder, 200);
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

    private Long longValue(JsonNode response, String pointer) {
        JsonNode node = response.at(pointer);
        assertFalse(node.isMissingNode(), "missing json path: " + pointer + " in " + response);
        return node.asLong();
    }
}
