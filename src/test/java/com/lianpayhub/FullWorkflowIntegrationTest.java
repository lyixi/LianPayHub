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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-full-flow-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-full-workflow-test",
        "lianpayhub.security.sms-code-required=false",
        "lianpayhub.security.api-auth-enabled=false",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456",
        "lianpayhub.payment.dev-tools-enabled=true",
        "lianpayhub.storage.local-path=./target/test-storage",
        "lianpayhub.storage.local-base-url=http://localhost:8888/files"
})
@AutoConfigureMockMvc
public class FullWorkflowIntegrationTest {

    private static final String STANDARD_APP = "flow-standard-app";
    private static final String SECOND_APP = "flow-second-standard-app";
    private static final String MANUAL_BIND_APP = "flow-manual-bind-app";
    private static final String DEVICE_APP = "flow-device-app";
    private static final String MOBILE = "13800009999";
    private static final String STANDARD_DEVICE_CODE = "flow-standard-device-001";
    private static final String DEVICE_ONLY_CODE = "flow-device-only-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void fullManagementAndApiWorkflowShouldPassOnInMemoryDatabase() throws Exception {
        mockMvc.perform(get("/console/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/console/app.js"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/console"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/console/"));
        mockMvc.perform(get("/admin-ui/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/console/"));

        JsonNode unauthenticated = requestJson(get("/admin/auth/me"), null, null, 401);
        assertCode(unauthenticated, 401);
        JsonNode expiredAdminToken = requestJson(get("/admin/auth/me"), "expired-or-invalid-token", null, 401);
        assertCode(expiredAdminToken, 401);

        JsonNode login = postOk("/admin/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"admin123456\"}");
        String token = text(login, "/data/token");
        Long adminId = longValue(login, "/data/adminId");

        JsonNode profile = getOk("/admin/auth/me", token);
        assertEquals(adminId.longValue(), longValue(profile, "/data/adminId").longValue());

        JsonNode operator = postOk("/admin/admin-users", token,
                "{\"username\":\"flow_operator\",\"password\":\"operator123456\",\"displayName\":\"Flow Operator\"}");
        Long operatorId = longValue(operator, "/data/id");
        assertFalse(operator.toString().contains("operator123456"));

        getOk("/admin/admin-users", token, "username", "flow_operator", "size", "20");
        JsonNode operatorDetail = getOk("/admin/admin-users/" + operatorId, token);
        assertEquals("flow_operator", text(operatorDetail, "/data/username"));

        JsonNode updatedOperator = putOk("/admin/admin-users/" + operatorId, token,
                "{\"displayName\":\"Flow Operator Updated\"}");
        assertEquals("Flow Operator Updated", text(updatedOperator, "/data/displayName"));

        postOk("/admin/admin-users/" + operatorId + "/reset-password", token,
                "{\"newPassword\":\"operatorReset123\"}");
        postOk("/admin/auth/login", null,
                "{\"username\":\"flow_operator\",\"password\":\"operatorReset123\"}");

        patchOk("/admin/admin-users/" + operatorId + "/status", token,
                "{\"status\":\"DISABLED\"}");
        postCode("/admin/auth/login", null,
                "{\"username\":\"flow_operator\",\"password\":\"operatorReset123\"}", 400);
        patchOk("/admin/admin-users/" + operatorId + "/status", token,
                "{\"status\":\"ENABLED\"}");

        patchCode("/admin/admin-users/" + adminId + "/status", token,
                "{\"status\":\"DISABLED\"}", 409);
        postOk("/admin/admin-users/me/change-password", token,
                "{\"oldPassword\":\"admin123456\",\"newPassword\":\"adminFlow123456\"}");
        postCode("/admin/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"admin123456\"}", 400);
        JsonNode relogin = postOk("/admin/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"adminFlow123456\"}");
        token = text(relogin, "/data/token");

        Long standardAppId = createApp(token, STANDARD_APP, "Flow Standard App", "STANDARD", true, false);
        Long secondAppId = createApp(token, SECOND_APP, "Flow Second Standard App", "STANDARD", true, false);
        Long manualBindAppId = createApp(token, MANUAL_BIND_APP, "Flow Manual Bind App", "STANDARD", false, false);
        Long deviceAppId = createApp(token, DEVICE_APP, "Flow Device App", "DEVICE_ONLY", false, true);
        assertTrue(secondAppId > 0L);
        assertTrue(manualBindAppId > 0L);

        JsonNode appList = getOk("/admin/apps", token);
        assertEquals(deviceAppId.longValue(), findArrayItem(appList.path("data"), "appId", DEVICE_APP).path("id").asLong());
        JsonNode updatedApp = putOk("/admin/apps/" + standardAppId, token,
                "{\"appName\":\"Flow Standard App Updated\",\"needMobileLogin\":true,\"needDeviceVip\":true}");
        assertEquals("Flow Standard App Updated", text(updatedApp, "/data/appName"));
        patchOk("/admin/apps/" + standardAppId + "/status", token, "{\"status\":\"DISABLED\"}");
        patchOk("/admin/apps/" + standardAppId + "/status", token, "{\"status\":\"ENABLED\"}");
        JsonNode resetSecret = postOk("/admin/apps/" + standardAppId + "/reset-secret", token, null);
        assertEquals(2, intValue(resetSecret, "/data/appSecretVersion"));
        assertTrue(text(resetSecret, "/data/appSecret").length() > 10);
        postCode("/admin/apps", token,
                "{\"appId\":\"" + STANDARD_APP + "\",\"appName\":\"Duplicate\",\"appType\":\"STANDARD\",\"needMobileLogin\":true,\"needDeviceVip\":false}",
                409);

        Long standardPackageId = createPackage(token, STANDARD_APP, "Flow Account Month", 1299, 31);
        Long devicePackageId = createPackage(token, DEVICE_APP, "Flow Device Month", 990, 30);
        JsonNode packageList = getOk("/admin/packages", token, "appId", DEVICE_APP);
        assertEquals(devicePackageId.longValue(), packageList.path("data").get(0).path("id").asLong());
        JsonNode updatedPackage = putOk("/admin/packages/" + devicePackageId, token,
                "{\"packageName\":\"Flow Device Month Updated\",\"priceCents\":1090,\"durationDays\":30,\"benefitsText\":\"device vip updated\"}");
        assertEquals(1090, intValue(updatedPackage, "/data/priceCents"));
        patchOk("/admin/packages/" + devicePackageId + "/status", token, "{\"status\":\"DISABLED\"}");

        JsonNode deviceBeforeOrder = postOk("/api/device/register", null,
                "{\"appId\":\"" + DEVICE_APP + "\",\"deviceCode\":\"" + DEVICE_ONLY_CODE + "\",\"deviceName\":\"Device Only\",\"deviceType\":\"android\",\"deviceFingerprint\":\"fingerprint-device-only\"}");
        Long deviceOnlyId = longValue(deviceBeforeOrder, "/data/id");
        postCode("/api/payment/create-order", null,
                "{\"appId\":\"" + DEVICE_APP + "\",\"deviceId\":" + deviceOnlyId + ",\"packageId\":" + devicePackageId + ",\"payChannel\":\"AGGREGATE\"}",
                409);
        patchOk("/admin/packages/" + devicePackageId + "/status", token, "{\"status\":\"ENABLED\"}");

        JsonNode paymentConfig = postOk("/admin/payment-configs", token,
                "{\"appId\":\"" + DEVICE_APP + "\",\"payChannel\":\"AGGREGATE\",\"providerCode\":\"flow-aggregate\",\"merchantId\":\"MCH-FLOW\",\"channelAppId\":\"APP-FLOW\",\"notifyUrl\":\"https://example.com/flow/notify\",\"configJson\":\"{\\\"mode\\\":\\\"test\\\"}\",\"credentialJson\":\"{\\\"privateKey\\\":\\\"flow-secret-key\\\"}\"}");
        Long paymentConfigId = longValue(paymentConfig, "/data/id");
        assertTrue(paymentConfig.path("data").path("credentialConfigured").asBoolean());
        assertFalse(paymentConfig.path("data").has("credentialJson"));
        assertFalse(paymentConfig.toString().contains("flow-secret-key"));
        JsonNode paymentConfigDetail = getOk("/admin/payment-configs/" + paymentConfigId, token);
        assertFalse(paymentConfigDetail.path("data").has("credentialJson"));
        getOk("/admin/payment-configs", token, "appId", DEVICE_APP, "payChannel", "AGGREGATE", "size", "20");
        JsonNode updatedPaymentConfig = putOk("/admin/payment-configs/" + paymentConfigId, token,
                "{\"providerCode\":\"flow-aggregate-updated\",\"merchantId\":\"MCH-FLOW-2\",\"channelAppId\":\"APP-FLOW-2\",\"notifyUrl\":\"https://example.com/flow/notify2\",\"configJson\":\"{\\\"mode\\\":\\\"updated\\\"}\",\"credentialJson\":\"\"}");
        assertEquals("flow-aggregate-updated", text(updatedPaymentConfig, "/data/providerCode"));
        postCode("/admin/payment-configs", token,
                "{\"appId\":\"" + DEVICE_APP + "\",\"payChannel\":\"AGGREGATE\",\"providerCode\":\"duplicate\"}",
                409);
        patchOk("/admin/payment-configs/" + paymentConfigId + "/status", token, "{\"status\":\"DISABLED\"}");
        postCode("/api/payment/create-order", null,
                "{\"appId\":\"" + DEVICE_APP + "\",\"deviceId\":" + deviceOnlyId + ",\"packageId\":" + devicePackageId + ",\"payChannel\":\"AGGREGATE\"}",
                409);
        patchOk("/admin/payment-configs/" + paymentConfigId + "/status", token, "{\"status\":\"ENABLED\"}");

        JsonNode smsConfig = postOk("/admin/notification-configs", token,
                "{\"channelType\":\"SMS\",\"providerCode\":\"local\",\"displayName\":\"Flow Local SMS\","
                        + "\"senderName\":\"LianPayHub\",\"senderAddress\":\"LianPayHub\","
                        + "\"configJson\":\"{}\","
                        + "\"credentialJson\":\"\"}");
        Long smsConfigId = longValue(smsConfig, "/data/id");
        assertFalse(smsConfig.path("data").path("credentialConfigured").asBoolean());
        assertFalse(smsConfig.path("data").has("credentialJson"));
        getOk("/admin/notification-configs", token, "channelType", "SMS", "providerCode", "local", "size", "20");
        JsonNode updatedSmsConfig = putOk("/admin/notification-configs/" + smsConfigId, token,
                "{\"providerCode\":\"local\",\"displayName\":\"Flow Local SMS Updated\","
                        + "\"senderName\":\"LianPayHub\",\"senderAddress\":\"LianPayHub\","
                        + "\"configJson\":\"{}\","
                        + "\"credentialJson\":\"\"}");
        assertEquals("Flow Local SMS Updated", text(updatedSmsConfig, "/data/displayName"));
        postOk("/admin/notification-configs/sms/send", token,
                "{\"configId\":" + smsConfigId + ",\"appId\":\"" + STANDARD_APP
                        + "\",\"mobile\":\"13800008888\",\"content\":\"flow sms test\"}");
        patchOk("/admin/notification-configs/" + smsConfigId + "/status", token, "{\"status\":\"DISABLED\"}");
        postCode("/admin/notification-configs/sms/send", token,
                "{\"configId\":" + smsConfigId + ",\"mobile\":\"13800008888\",\"content\":\"disabled sms\"}",
                409);
        patchOk("/admin/notification-configs/" + smsConfigId + "/status", token, "{\"status\":\"ENABLED\"}");

        JsonNode emailConfig = postOk("/admin/notification-configs", token,
                "{\"channelType\":\"EMAIL\",\"providerCode\":\"local\",\"displayName\":\"Flow Local Mail\","
                        + "\"senderName\":\"LianPayHub\",\"senderAddress\":\"noreply@example.com\","
                        + "\"configJson\":\"{}\","
                        + "\"credentialJson\":\"{\\\"password\\\":\\\"flow-mail-secret\\\"}\"}");
        Long emailConfigId = longValue(emailConfig, "/data/id");
        assertFalse(emailConfig.path("data").has("credentialJson"));
        postOk("/admin/notification-configs/email/send", token,
                "{\"configId\":" + emailConfigId + ",\"to\":\"flow@example.com\","
                        + "\"subject\":\"Flow Mail\",\"content\":\"flow mail test\",\"html\":false}");
        mockMvc.perform(get("/admin/exports/notification-configs")
                        .header("Authorization", "Bearer " + token)
                        .param("channelType", "SMS"))
                .andExpect(status().isOk());

        postOk("/api/auth/send-code", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\"}");
        JsonNode appLogin = postOk("/api/auth/login", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\",\"code\":\"000000\"}");
        Long userId = longValue(appLogin, "/data/userId");
        String appUserToken = text(appLogin, "/data/token");
        assertTrue(appUserToken.length() > 10);

        JsonNode secondAppLogin = postOk("/api/auth/login", null,
                "{\"appId\":\"" + SECOND_APP + "\",\"mobile\":\"" + MOBILE + "\"}");
        assertEquals(userId.longValue(), longValue(secondAppLogin, "/data/userId").longValue());
        postCode("/api/auth/login", null,
                "{\"appId\":\"" + DEVICE_APP + "\",\"mobile\":\"13800008888\"}", 409);

        JsonNode userList = getOk("/admin/users", token, "mobile", MOBILE, "size", "20");
        assertEquals(userId.longValue(), firstPageItem(userList).path("id").asLong());
        patchOk("/admin/users/" + userId + "/status", token, "{\"status\":\"DISABLED\"}");
        postCode("/api/auth/login", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"mobile\":\"" + MOBILE + "\"}", 409);
        patchOk("/admin/users/" + userId + "/status", token, "{\"status\":\"ENABLED\"}");

        JsonNode bindingList = getOk("/admin/user-bindings", token,
                "appId", STANDARD_APP, "userId", String.valueOf(userId), "size", "20");
        Long standardBindingId = firstPageItem(bindingList).path("id").asLong();
        getOk("/admin/user-bindings/" + standardBindingId, token);
        patchOk("/admin/user-bindings/" + standardBindingId + "/status", token, "{\"status\":\"DISABLED\"}");
        patchOk("/admin/user-bindings/" + standardBindingId + "/status", token, "{\"status\":\"ENABLED\"}");
        JsonNode manualBinding = postOk("/admin/user-bindings", token,
                "{\"userId\":" + userId + ",\"appId\":\"" + MANUAL_BIND_APP + "\",\"bindType\":\"DEVICE_BIND\"}");
        Long manualBindingId = longValue(manualBinding, "/data/id");
        postCode("/admin/user-bindings", token,
                "{\"userId\":" + userId + ",\"appId\":\"" + MANUAL_BIND_APP + "\",\"bindType\":\"DEVICE_BIND\"}",
                409);
        getOk("/admin/user-bindings/" + manualBindingId, token);

        JsonNode standardDevice = postOk("/api/device/register", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"deviceCode\":\"" + STANDARD_DEVICE_CODE + "\",\"deviceName\":\"Standard Device\",\"deviceType\":\"ios\",\"deviceFingerprint\":\"fingerprint-standard\"}");
        Long standardDeviceId = longValue(standardDevice, "/data/id");
        getOk("/admin/devices/" + standardDeviceId, token);
        JsonNode deviceList = getOk("/admin/devices", token,
                "appId", STANDARD_APP, "deviceCode", STANDARD_DEVICE_CODE, "size", "20");
        assertEquals(standardDeviceId.longValue(), firstPageItem(deviceList).path("id").asLong());
        JsonNode boundDevice = postOk("/admin/devices/" + standardDeviceId + "/bind-user", token,
                "{\"userId\":" + userId + "}");
        assertEquals("BOUND", text(boundDevice, "/data/bindStatus"));
        JsonNode unboundDevice = postOk("/admin/devices/" + standardDeviceId + "/unbind", token, null);
        assertEquals("UNBOUND", text(unboundDevice, "/data/bindStatus"));
        postOk("/api/device/launch", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"deviceCode\":\"" + STANDARD_DEVICE_CODE + "\",\"userId\":" + userId + ",\"platform\":\"ios\",\"version\":\"1.0.0\",\"networkType\":\"wifi\",\"ipAddress\":\"127.0.0.1\",\"eventData\":\"{\\\"scene\\\":\\\"full-flow\\\"}\"}");
        JsonNode launchList = getOk("/admin/launch-records", token,
                "appId", STANDARD_APP, "deviceId", String.valueOf(standardDeviceId), "size", "20");
        Long launchId = firstPageItem(launchList).path("id").asLong();
        getOk("/admin/launch-records/" + launchId, token);
        getOk("/admin/logs/launches", token, "appId", STANDARD_APP, "size", "20");

        JsonNode inactiveMember = getOk("/api/member/status", null,
                "appId", STANDARD_APP, "userId", String.valueOf(userId));
        assertFalse(inactiveMember.path("data").path("active").asBoolean());
        JsonNode grantedMember = postOk("/admin/members/grant", token,
                "{\"appId\":\"" + STANDARD_APP + "\",\"subjectType\":\"USER\",\"userId\":" + userId + ",\"packageId\":" + standardPackageId + ",\"durationDays\":7}");
        Long grantedMemberId = longValue(grantedMember, "/data/id");
        JsonNode activeMember = getOk("/api/member/status", null,
                "appId", STANDARD_APP, "userId", String.valueOf(userId));
        assertTrue(activeMember.path("data").path("active").asBoolean());
        getOk("/admin/members", token, "appId", STANDARD_APP, "size", "20");
        JsonNode cancelledMember = postOk("/admin/members/" + grantedMemberId + "/cancel", token, null);
        assertEquals("CANCELLED", text(cancelledMember, "/data/status"));

        JsonNode accountOrder = postOk("/api/payment/create-order", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"userId\":" + userId + ",\"packageId\":" + standardPackageId + ",\"payChannel\":\"WECHAT\"}");
        String accountOrderNo = text(accountOrder, "/data/orderNo");
        assertFalse(accountOrder.path("data").path("paymentParams").path("configured").asBoolean());
        Long accountOrderId = findOrderIdByOrderNo(token, STANDARD_APP, accountOrderNo);
        postOk("/admin/orders/" + accountOrderId + "/mark-paid", token,
                "{\"tradeNo\":\"FLOW-MANUAL-TRADE\"}");
        JsonNode paidAccountOrder = getOk("/admin/orders/" + accountOrderId, token);
        assertEquals("PAID", text(paidAccountOrder, "/data/payStatus"));

        JsonNode failedRefund = postOk("/admin/refunds", token,
                "{\"orderId\":" + accountOrderId + ",\"amountCents\":100,\"reason\":\"flow failed refund\"}");
        Long failedRefundId = longValue(failedRefund, "/data/id");
        JsonNode failedRefundResult = postOk("/admin/refunds/" + failedRefundId + "/mark-failed", token, null);
        assertEquals("FAILED", text(failedRefundResult, "/data/status"));

        JsonNode deviceOrder = postOk("/api/payment/create-order", null,
                "{\"appId\":\"" + DEVICE_APP + "\",\"deviceId\":" + deviceOnlyId + ",\"packageId\":" + devicePackageId + ",\"payChannel\":\"AGGREGATE\"}");
        String deviceOrderNo = text(deviceOrder, "/data/orderNo");
        assertEquals("flow-aggregate-updated", text(deviceOrder, "/data/paymentParams/provider"));
        assertTrue(deviceOrder.path("data").path("paymentParams").path("configured").asBoolean());
        Long deviceOrderId = findOrderIdByOrderNo(token, DEVICE_APP, deviceOrderNo);

        postCode("/api/payment/notify/AGGREGATE", null,
                "{\"payload\":\"verified=false&orderNo=" + deviceOrderNo + "&tradeNo=FLOW-TRADE-FAIL&status=FAILED\"}",
                400);
        JsonNode notifySuccess = postOk("/api/payment/notify/AGGREGATE", null,
                "{\"payload\":\"verified=true&orderNo=" + deviceOrderNo + "&tradeNo=FLOW-TRADE-001&status=SUCCESS\"}");
        assertTrue(notifySuccess.path("data").path("processed").asBoolean());
        JsonNode notifyDuplicate = postOk("/api/payment/notify/AGGREGATE", null,
                "{\"payload\":\"verified=true&orderNo=" + deviceOrderNo + "&tradeNo=FLOW-TRADE-001&status=SUCCESS\"}");
        assertFalse(notifyDuplicate.path("data").path("processed").asBoolean());

        JsonNode deviceMemberStatus = getOk("/api/member/status", null,
                "appId", DEVICE_APP, "deviceId", String.valueOf(deviceOnlyId));
        assertTrue(deviceMemberStatus.path("data").path("active").asBoolean());
        JsonNode deviceMemberByCode = getOk("/api/member/status", null,
                "appId", DEVICE_APP, "deviceCode", DEVICE_ONLY_CODE);
        assertTrue(deviceMemberByCode.path("data").path("active").asBoolean());

        getOk("/admin/orders/" + deviceOrderId, token);
        getOk("/admin/payment-callbacks", token, "appId", DEVICE_APP, "orderId", String.valueOf(deviceOrderId), "size", "20");
        getOk("/admin/logs/payment-events", token, "appId", DEVICE_APP, "orderId", String.valueOf(deviceOrderId), "size", "20");
        JsonNode successfulRefund = postOk("/admin/refunds", token,
                "{\"orderId\":" + deviceOrderId + ",\"amountCents\":100,\"reason\":\"flow success refund\"}");
        Long successfulRefundId = longValue(successfulRefund, "/data/id");
        JsonNode refundSuccess = postOk("/admin/refunds/" + successfulRefundId + "/mark-success", token,
                "{\"channelRefundNo\":\"RF-FLOW-001\"}");
        assertEquals("SUCCESS", text(refundSuccess, "/data/status"));
        postCode("/admin/refunds/" + successfulRefundId + "/mark-success", token,
                "{\"channelRefundNo\":\"RF-FLOW-002\"}", 409);
        getOk("/admin/refunds", token, "appId", DEVICE_APP, "size", "20");
        getOk("/admin/payment-refunds", token, "appId", DEVICE_APP, "orderId", String.valueOf(deviceOrderId), "size", "20");

        JsonNode adapterStatus = getOk("/api/adapter/status", null, "appId", STANDARD_APP);
        assertEquals("UP", text(adapterStatus, "/data"));
        postOk("/api/adapter/report", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"sourceId\":\"source-flow-1\",\"reportType\":\"HEALTH\",\"payload\":\"{\\\"ok\\\":true}\"}");
        JsonNode adapterReports = getOk("/admin/adapter-reports", token,
                "appId", STANDARD_APP, "sourceId", "source-flow-1", "size", "20");
        Long adapterReportId = firstPageItem(adapterReports).path("id").asLong();
        getOk("/admin/adapter-reports/" + adapterReportId, token);
        JsonNode processedReport = postOk("/admin/adapter-reports/" + adapterReportId + "/mark-processed", token, null);
        assertEquals("PROCESSED", text(processedReport, "/data/status"));
        postOk("/api/adapter/report", null,
                "{\"appId\":\"" + STANDARD_APP + "\",\"sourceId\":\"source-flow-2\",\"reportType\":\"ERROR\",\"payload\":\"{\\\"message\\\":\\\"sample\\\"}\"}");
        JsonNode failedReports = getOk("/admin/adapter-reports", token,
                "appId", STANDARD_APP, "sourceId", "source-flow-2", "size", "20");
        Long failedReportId = firstPageItem(failedReports).path("id").asLong();
        JsonNode failedReport = postOk("/admin/adapter-reports/" + failedReportId + "/mark-failed", token, null);
        assertEquals("FAILED", text(failedReport, "/data/status"));

        JsonNode demo = postOk("/admin/demo/device-vip", token, null);
        assertEquals("demo-device-app", text(demo, "/data/appId"));
        assertTrue(text(demo, "/data/orderNo").startsWith("LFP"));

        getOk("/admin/reports/overview", token);
        JsonNode trend = getOk("/admin/reports/trend", token, "days", "7");
        assertTrue(trend.path("data").isArray());
        JsonNode paidAmountAnalytics = getOk("/admin/reports/analytics", token,
                "granularity", "DAY", "metric", "PAID_AMOUNT", "appId", DEVICE_APP, "periods", "7");
        assertEquals("PAID_AMOUNT", text(paidAmountAnalytics, "/data/metric"));
        assertEquals(DEVICE_APP, text(paidAmountAnalytics, "/data/appId"));
        assertTrue(paidAmountAnalytics.path("data").path("points").isArray());
        JsonNode monthlyLaunchAnalytics = getOk("/admin/reports/analytics", token,
                "granularity", "MONTH", "metric", "LAUNCH_COUNT", "periods", "3");
        assertEquals("MONTH", text(monthlyLaunchAnalytics, "/data/granularity"));
        assertTrue(monthlyLaunchAnalytics.path("data").path("points").size() <= 3);
        JsonNode paymentSummary = getOk("/admin/reports/payment-summary", token);
        assertEquals(DEVICE_APP, text(findArrayItem(paymentSummary.path("data").path("byApp"), "dimension", DEVICE_APP), "/dimension"));
        assertEquals("AGGREGATE", text(findArrayItem(paymentSummary.path("data").path("byPayChannel"), "dimension", "AGGREGATE"), "/dimension"));

        // === 云同步文件系统 ===

        // 未认证访问返回 401
        requestJson(get("/api/sync/list"), null, null, 401);

        // 上传 JSON 配置文件
        JsonNode uploadConfig = syncUpload(appUserToken, "/settings/config.json", "config.json",
                "application/json", "{\"theme\":\"dark\"}".getBytes());
        assertCode(uploadConfig, 0);
        Long configFileId = longValue(uploadConfig, "/data/id");
        assertEquals("/settings/config.json", text(uploadConfig, "/data/virtualPath"));
        assertEquals("CONFIG", text(uploadConfig, "/data/fileCategory"));
        assertEquals(1L, longValue(uploadConfig, "/data/version").longValue());

        // 同路径覆盖写入，version 自增，id 不变
        JsonNode overwrittenConfig = syncUpload(appUserToken, "/settings/config.json", "config.json",
                "application/json", "{\"theme\":\"light\"}".getBytes());
        assertCode(overwrittenConfig, 0);
        assertEquals(configFileId.longValue(), longValue(overwrittenConfig, "/data/id").longValue());
        assertEquals(2L, longValue(overwrittenConfig, "/data/version").longValue());

        // 上传有效图片（最小 1×1 PNG）
        JsonNode uploadPng = syncUpload(appUserToken, "/images/icon.png", "icon.png",
                "image/png", generateMinimalPng());
        assertCode(uploadPng, 0);
        Long pngFileId = longValue(uploadPng, "/data/id");
        assertEquals("IMAGE", text(uploadPng, "/data/fileCategory"));

        // 拒绝不支持的文件类型（.exe）
        JsonNode rejectedExe = syncUpload(appUserToken, "/bad/virus.exe", "virus.exe",
                "application/octet-stream", new byte[]{0x4D, 0x5A, 0x00, 0x00});
        assertCode(rejectedExe, 400);

        // 拒绝 Magic Bytes 不匹配（文本内容假冒 .png 扩展名）
        JsonNode fakePng = syncUpload(appUserToken, "/bad/fake.png", "fake.png",
                "image/png", "not-a-real-image-content".getBytes());
        assertCode(fakePng, 400);

        // 列出目录（/settings 下应有 config.json）
        JsonNode settingsList = getOk("/api/sync/list", appUserToken, "path", "/settings");
        assertTrue(settingsList.path("data").isArray());
        assertEquals(1, settingsList.path("data").size());
        assertEquals("/settings/config.json", settingsList.path("data").get(0).path("virtualPath").asText());

        // 获取限时下载 URL
        JsonNode downloadResult = getOk("/api/sync/" + configFileId + "/url", appUserToken);
        assertTrue(text(downloadResult, "/data/url").length() > 0);
        assertEquals(900L, longValue(downloadResult, "/data/expiresInSeconds").longValue());

        // 全量同步（since=0），应包含 config.json 和 icon.png
        JsonNode allChanges = getOk("/api/sync/changes", appUserToken, "since", "0");
        assertTrue(allChanges.path("data").path("changes").isArray());
        assertTrue(allChanges.path("data").path("changes").size() >= 2);
        assertTrue(allChanges.path("data").path("syncTimestamp").asLong() > 0);

        // 删除配置文件（软删除）
        JsonNode deleteResp = requestJson(delete("/api/sync/" + configFileId), appUserToken, null, 200);
        assertCode(deleteResp, 0);

        // 全量同步：deleted 的文件 deleted 字段应为 true
        JsonNode afterDeleteChanges = getOk("/api/sync/changes", appUserToken, "since", "0");
        boolean foundDeleted = false;
        for (JsonNode change : afterDeleteChanges.path("data").path("changes")) {
            if (configFileId.longValue() == change.path("id").asLong()) {
                assertTrue(change.path("deleted").asBoolean(),
                        "config.json 应标记为 deleted");
                foundDeleted = true;
            }
        }
        assertTrue(foundDeleted, "已删除的文件应出现在 changes 列表中");

        // 已删除文件不再出现在目录列表中
        JsonNode settingsListAfterDelete = getOk("/api/sync/list", appUserToken, "path", "/settings");
        assertEquals(0, settingsListAfterDelete.path("data").size());

        // === 云同步文件系统结束 ===

        getOk("/admin/logs/app-logins", token, "appId", STANDARD_APP, "mobile", MOBILE, "size", "20");
        JsonNode operationLogs = getOk("/admin/logs/admin-operations", token,
                "adminId", String.valueOf(adminId), "size", "100");
        String operationLogBody = operationLogs.toString();
        assertFalse(operationLogBody.contains("operator123456"));
        assertFalse(operationLogBody.contains("operatorReset123"));
        assertFalse(operationLogBody.contains("adminFlow123456"));
        assertFalse(operationLogBody.contains("flow-secret-key"));
        assertFalse(operationLogBody.contains("flow-mail-secret"));
        assertNotEquals(0, operationLogs.path("data").path("content").size());
    }

    private Long createApp(String token, String appId, String appName, String appType,
                           boolean needMobileLogin, boolean needDeviceVip) throws Exception {
        JsonNode response = postOk("/admin/apps", token,
                "{\"appId\":\"" + appId + "\",\"appName\":\"" + appName + "\",\"appType\":\"" + appType
                        + "\",\"needMobileLogin\":" + needMobileLogin + ",\"needDeviceVip\":" + needDeviceVip + "}");
        assertEquals(appId, text(response, "/data/appId"));
        assertTrue(text(response, "/data/appSecret").length() > 10);
        return longValue(response, "/data/id");
    }

    private Long createPackage(String token, String appId, String packageName, int priceCents,
                               int durationDays) throws Exception {
        JsonNode response = postOk("/admin/packages", token,
                "{\"appId\":\"" + appId + "\",\"packageName\":\"" + packageName
                        + "\",\"packageType\":\"MEMBERSHIP\",\"priceCents\":" + priceCents
                        + ",\"durationDays\":" + durationDays + ",\"benefitsText\":\"flow benefits\"}");
        assertEquals(appId, text(response, "/data/appId"));
        return longValue(response, "/data/id");
    }

    private Long findOrderIdByOrderNo(String token, String appId, String orderNo) throws Exception {
        JsonNode response = getOk("/admin/orders", token, "appId", appId, "size", "100");
        JsonNode content = response.path("data").path("content");
        for (JsonNode item : content) {
            if (orderNo.equals(item.path("orderNo").asText())) {
                return item.path("id").asLong();
            }
        }
        throw new AssertionError("Order not found: " + orderNo);
    }

    private JsonNode getOk(String path, String token, String... params) throws Exception {
        MockHttpServletRequestBuilder builder = get(path);
        applyParams(builder, params);
        return apiOk(requestJson(builder, token, null, 200));
    }

    private JsonNode postOk(String path, String token, String json) throws Exception {
        return apiOk(requestJson(post(path), token, json, 200));
    }

    private JsonNode putOk(String path, String token, String json) throws Exception {
        return apiOk(requestJson(put(path), token, json, 200));
    }

    private JsonNode patchOk(String path, String token, String json) throws Exception {
        return apiOk(requestJson(patch(path), token, json, 200));
    }

    private JsonNode postCode(String path, String token, String json, int code) throws Exception {
        JsonNode response = requestJson(post(path), token, json, 200);
        assertCode(response, code);
        return response;
    }

    private JsonNode patchCode(String path, String token, String json, int code) throws Exception {
        JsonNode response = requestJson(patch(path), token, json, 200);
        assertCode(response, code);
        return response;
    }

    private JsonNode requestJson(MockHttpServletRequestBuilder builder, String token, String json,
                                 int expectedHttpStatus) throws Exception {
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (json != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().is(expectedHttpStatus))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void applyParams(MockHttpServletRequestBuilder builder, String... params) {
        assertEquals(0, params.length % 2, "params must be key-value pairs");
        for (int i = 0; i < params.length; i += 2) {
            builder.param(params[i], params[i + 1]);
        }
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

    private int intValue(JsonNode response, String pointer) {
        JsonNode node = response.at(pointer);
        assertFalse(node.isMissingNode(), "missing json path: " + pointer + " in " + response);
        return node.asInt();
    }

    private JsonNode firstPageItem(JsonNode response) {
        JsonNode content = response.path("data").path("content");
        assertTrue(content.isArray(), response.toString());
        assertTrue(content.size() > 0, response.toString());
        return content.get(0);
    }

    private JsonNode findArrayItem(JsonNode array, String field, String value) {
        assertTrue(array.isArray(), array.toString());
        for (JsonNode item : array) {
            if (value.equals(item.path(field).asText())) {
                return item;
            }
        }
        throw new AssertionError("Item not found: " + field + "=" + value);
    }

    /** 上传文件到 /api/sync/upload，返回 ApiResponse JSON（HTTP 始终 200，错误体现在 code 字段）。 */
    private JsonNode syncUpload(String token, String path, String filename,
                                String contentType, byte[] content) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
        MvcResult result = mockMvc.perform(
                        multipart("/api/sync/upload")
                                .file(file)
                                .param("path", path)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** 生成最小有效 1×1 PNG（用于测试图片上传校验通过路径）。 */
    private byte[] generateMinimalPng() throws Exception {
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
