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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-smoke-test",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class AdminApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void adminPaymentFlowShouldBindAndSerializeJsonBody() throws Exception {
        mockMvc.perform(get("/console/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/console/app.js"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin-ui/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/console/"));

        MvcResult loginResult = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginBody.path("data").path("token").asText();
        Long currentAdminId = loginBody.path("data").path("adminId").asLong();

        MvcResult createdAdminResult = mockMvc.perform(post("/admin/admin-users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operator\",\"password\":\"operator123\",\"displayName\":\"运营\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("operator"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();
        Long operatorId = objectMapper.readTree(createdAdminResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/admin/admin-users/" + operatorId + "/reset-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newOperator123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operator\",\"password\":\"newOperator123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        MvcResult operationLogResult = mockMvc.perform(get("/admin/logs/admin-operations")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        String operationLogBody = operationLogResult.getResponse().getContentAsString();
        assertFalse(operationLogBody.contains("operator123"));
        assertFalse(operationLogBody.contains("newOperator123"));

        mockMvc.perform(patch("/admin/admin-users/" + currentAdminId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));

        mockMvc.perform(post("/admin/apps")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-standard-app\",\"appName\":\"烟测标准 APP\",\"appType\":\"STANDARD\",\"needMobileLogin\":true,\"needDeviceVip\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult appLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-standard-app\",\"mobile\":\"13800000001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andReturn();
        Long userId = objectMapper.readTree(appLoginResult.getResponse().getContentAsString())
                .path("data").path("userId").asLong();

        MvcResult bindingResult = mockMvc.perform(get("/admin/user-bindings")
                        .header("Authorization", "Bearer " + token)
                        .param("appId", "smoke-standard-app")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content[0].appId").value("smoke-standard-app"))
                .andReturn();
        Long bindingId = objectMapper.readTree(bindingResult.getResponse().getContentAsString())
                .path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(patch("/admin/user-bindings/" + bindingId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(patch("/admin/user-bindings/" + bindingId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult standardDeviceResult = mockMvc.perform(post("/api/device/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-standard-app\",\"deviceCode\":\"standard-device-001\",\"deviceName\":\"标准设备\",\"deviceType\":\"ios\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        Long standardDeviceId = objectMapper.readTree(standardDeviceResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/admin/devices/" + standardDeviceId + "/bind-user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + userId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.bindStatus").value("BOUND"));

        mockMvc.perform(post("/admin/apps")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"appName\":\"烟测设备 APP\",\"appType\":\"DEVICE_ONLY\",\"needMobileLogin\":false,\"needDeviceVip\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.appId").value("smoke-device-app"))
                .andExpect(jsonPath("$.data.appSecret").isNotEmpty());

        MvcResult paymentConfigResult = mockMvc.perform(post("/admin/payment-configs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"payChannel\":\"AGGREGATE\",\"providerCode\":\"mock-aggregate\",\"merchantId\":\"MCH-001\",\"channelAppId\":\"APP-001\",\"notifyUrl\":\"https://example.com/pay/notify\",\"configJson\":\"{\\\"mode\\\":\\\"test\\\"}\",\"credentialJson\":\"{\\\"privateKey\\\":\\\"secret-key\\\"}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.providerCode").value("mock-aggregate"))
                .andExpect(jsonPath("$.data.credentialConfigured").value(true))
                .andExpect(jsonPath("$.data.credentialJson").doesNotExist())
                .andReturn();
        Long paymentConfigId = objectMapper.readTree(paymentConfigResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        MvcResult paymentConfigLogResult = mockMvc.perform(get("/admin/logs/admin-operations")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        assertFalse(paymentConfigLogResult.getResponse().getContentAsString().contains("secret-key"));

        MvcResult packageResult = mockMvc.perform(post("/admin/packages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"packageName\":\"月度会员\",\"packageType\":\"MEMBERSHIP\",\"priceCents\":990,\"durationDays\":30,\"benefitsText\":\"VIP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        Long packageId = objectMapper.readTree(packageResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        MvcResult deviceResult = mockMvc.perform(post("/api/device/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"deviceCode\":\"device-001\",\"deviceName\":\"测试设备\",\"deviceType\":\"android\",\"deviceFingerprint\":\"fingerprint-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        Long deviceId = objectMapper.readTree(deviceResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(patch("/admin/payment-configs/" + paymentConfigId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"deviceId\":" + deviceId
                                + ",\"packageId\":" + packageId + ",\"payChannel\":\"AGGREGATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));

        mockMvc.perform(patch("/admin/payment-configs/" + paymentConfigId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult orderResult = mockMvc.perform(post("/api/payment/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"smoke-device-app\",\"deviceId\":" + deviceId
                                + ",\"packageId\":" + packageId + ",\"payChannel\":\"AGGREGATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.paymentParams.provider").value("mock-aggregate"))
                .andExpect(jsonPath("$.data.paymentParams.configured").value(true))
                .andReturn();
        String orderNo = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .path("data").path("orderNo").asText();

        mockMvc.perform(post("/api/payment/notify/AGGREGATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payload\":\"verified=true&orderNo=" + orderNo + "&tradeNo=SMOKE-001&status=SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.processed").value(true));

        mockMvc.perform(get("/api/member/status")
                        .param("appId", "smoke-device-app")
                        .param("deviceId", String.valueOf(deviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.active").value(true));

        MvcResult orderListResult = mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + token)
                        .param("appId", "smoke-device-app")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long orderId = objectMapper.readTree(orderListResult.getResponse().getContentAsString())
                .path("data").path("content").get(0).path("id").asLong();

        MvcResult refundResult = mockMvc.perform(post("/admin/refunds")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + orderId + ",\"amountCents\":100,\"reason\":\"smoke\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        Long refundId = objectMapper.readTree(refundResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/admin/refunds/" + refundId + "/mark-success")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channelRefundNo\":\"RF-SMOKE-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/admin/refunds/" + refundId + "/mark-success")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channelRefundNo\":\"RF-SMOKE-002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }
}
