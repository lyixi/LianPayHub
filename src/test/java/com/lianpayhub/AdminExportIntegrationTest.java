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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-export-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-export-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.api-auth-enabled=false",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
public class AdminExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void adminExportsShouldRequireTokenAndMaskSensitiveCredentials() throws Exception {
        mockMvc.perform(get("/admin/exports/users"))
                .andExpect(status().isUnauthorized());

        String token = adminToken();
        createApp(token, "export-standard-app", "STANDARD", true, false);
        createApp(token, "export-device-app", "DEVICE_ONLY", false, true);
        createPaymentConfig(token);
        Long packageId = createPackage(token);
        Long deviceId = createDevice();
        Long refundOrderId = createRefundOrder(token, deviceId, packageId);
        createRefund(token, refundOrderId);
        appLogin();
        launch(deviceId);
        adapterReport();
        String orderNo = createOrder(deviceId, packageId);
        Long orderId = findOrderId(token, orderNo);
        notify(orderNo);

        String appsCsv = csv("/admin/exports/apps", token);
        assertTrue(appsCsv.startsWith("id,appId,appName"), appsCsv);
        assertTrue(appsCsv.contains("export-device-app"), appsCsv);
        assertFalse(appsCsv.contains("appSecretHash"), appsCsv);

        String paymentConfigCsv = csv("/admin/exports/payment-configs?appId=export-device-app", token);
        assertTrue(paymentConfigCsv.contains("credentialConfigured"), paymentConfigCsv);
        assertTrue(paymentConfigCsv.contains("true"), paymentConfigCsv);
        assertFalse(paymentConfigCsv.contains("export-secret-key"), paymentConfigCsv);

        assertTrue(csv("/admin/exports/users?mobile=13800007777", token).contains("13800007777"));
        assertTrue(csv("/admin/exports/devices?appId=export-device-app", token).contains("export-device-001"));
        assertTrue(csv("/admin/exports/orders?appId=export-device-app", token).contains(orderNo));
        assertTrue(csv("/admin/exports/payment-refunds?appId=export-device-app", token).contains("export refund"));
        assertTrue(csv("/admin/exports/payment-callbacks?appId=export-device-app", token).contains("EXPORT-TRADE-001"));
        assertTrue(csv("/admin/exports/launch-records?appId=export-device-app", token).contains("export-device-app"));
        assertTrue(csv("/admin/exports/adapter-reports?appId=export-standard-app", token).contains("source-export-1"));
        assertTrue(csv("/admin/exports/logs/app-logins?appId=export-standard-app", token).contains("13800007777"));
        assertTrue(csv("/admin/exports/logs/payment-events?appId=export-device-app", token).contains("PAYMENT_SUCCESS"));

        String adminUsersCsv = csv("/admin/exports/admin-users?username=admin", token);
        assertTrue(adminUsersCsv.contains("admin"), adminUsersCsv);
        assertFalse(adminUsersCsv.contains("passwordHash"), adminUsersCsv);

        String adminLogsCsv = csv("/admin/exports/logs/admin-operations", token);
        assertTrue(adminLogsCsv.contains("operationType"), adminLogsCsv);
        assertFalse(adminLogsCsv.contains("export-secret-key"), adminLogsCsv);
    }

    private String csv(String path, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(path)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    private String adminToken() throws Exception {
        JsonNode response = request(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"));
        return response.path("data").path("token").asText();
    }

    private void createApp(String token, String appId, String appType, boolean needMobileLogin,
                           boolean needDeviceVip) throws Exception {
        request(post("/admin/apps")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"" + appId + "\",\"appName\":\"" + appId + "\",\"appType\":\"" + appType
                        + "\",\"needMobileLogin\":" + needMobileLogin + ",\"needDeviceVip\":" + needDeviceVip + "}"));
    }

    private void createPaymentConfig(String token) throws Exception {
        request(post("/admin/payment-configs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-device-app\",\"payChannel\":\"AGGREGATE\","
                        + "\"providerCode\":\"export-provider\",\"merchantId\":\"MCH-EXPORT\","
                        + "\"channelAppId\":\"APP-EXPORT\",\"notifyUrl\":\"https://example.com/export\","
                        + "\"configJson\":\"{\\\"mode\\\":\\\"test\\\"}\","
                        + "\"credentialJson\":\"{\\\"privateKey\\\":\\\"export-secret-key\\\"}\"}"));
    }

    private Long createPackage(String token) throws Exception {
        JsonNode response = request(post("/admin/packages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-device-app\",\"packageName\":\"Export Month\","
                        + "\"packageType\":\"MEMBERSHIP\",\"priceCents\":990,\"durationDays\":30,"
                        + "\"benefitsText\":\"export benefits\"}"));
        return response.path("data").path("id").asLong();
    }

    private Long createDevice() throws Exception {
        JsonNode response = request(post("/api/device/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-device-app\",\"deviceCode\":\"export-device-001\","
                        + "\"deviceName\":\"Export Device\",\"deviceType\":\"android\"}"));
        return response.path("data").path("id").asLong();
    }

    private String createOrder(Long deviceId, Long packageId) throws Exception {
        JsonNode response = request(post("/api/payment/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-device-app\",\"deviceId\":" + deviceId
                        + ",\"packageId\":" + packageId + ",\"payChannel\":\"AGGREGATE\"}"));
        return response.path("data").path("orderNo").asText();
    }

    private Long createRefundOrder(String token, Long deviceId, Long packageId) throws Exception {
        String orderNo = createOrder(deviceId, packageId);
        Long orderId = findOrderId(token, orderNo);
        request(post("/admin/orders/" + orderId + "/mark-paid")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tradeNo\":\"EXPORT-MANUAL-PAID\"}"));
        return orderId;
    }

    private Long findOrderId(String token, String orderNo) throws Exception {
        JsonNode response = request(get("/admin/orders")
                .header("Authorization", "Bearer " + token)
                .param("appId", "export-device-app")
                .param("size", "20"));
        JsonNode content = response.path("data").path("content");
        for (JsonNode item : content) {
            if (orderNo.equals(item.path("orderNo").asText())) {
                return item.path("id").asLong();
            }
        }
        throw new AssertionError("order not found: " + orderNo);
    }

    private void createRefund(String token, Long orderId) throws Exception {
        request(post("/admin/refunds")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"amountCents\":100,\"reason\":\"export refund\"}"));
    }

    private void appLogin() throws Exception {
        request(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-standard-app\",\"mobile\":\"13800007777\"}"));
    }

    private void launch(Long deviceId) throws Exception {
        request(post("/api/device/launch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-device-app\",\"deviceCode\":\"export-device-001\","
                        + "\"deviceId\":" + deviceId + ",\"platform\":\"android\",\"version\":\"1.0.0\"}"));
    }

    private void adapterReport() throws Exception {
        request(post("/api/adapter/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appId\":\"export-standard-app\",\"sourceId\":\"source-export-1\","
                        + "\"reportType\":\"HEALTH\",\"payload\":\"{\\\"ok\\\":true}\"}"));
    }

    private void notify(String orderNo) throws Exception {
        request(post("/api/payment/notify/AGGREGATE")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payload\":\"verified=true&orderNo=" + orderNo
                        + "&tradeNo=EXPORT-TRADE-001&status=SUCCESS\"}"));
    }

    private JsonNode request(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder)
            throws Exception {
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
