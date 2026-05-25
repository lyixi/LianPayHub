package com.lianpayhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.service.payment.PaymentService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
        "spring.datasource.url=jdbc:h2:mem:lianpayhub-order-lifecycle-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE",
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
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-order-lifecycle-test",
        "lianpayhub.security.api-auth-enabled=false",
        "lianpayhub.admin.default-username=admin",
        "lianpayhub.admin.default-password=admin123456",
        "lianpayhub.payment.order-expire-minutes=30"
})
@AutoConfigureMockMvc
public class PaymentOrderLifecycleIntegrationTest {

    private static final String APP_ID = "lifecycle-device-app";
    private static final String EXPIRED_APP_ID = "lifecycle-expired-device-app";
    private static final String DEVICE_CODE = "lifecycle-device-001";
    private static final String EXPIRED_DEVICE_CODE = "lifecycle-expired-device-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    public void pendingOrderCanBeClosedAndThenRejectsPaymentAndRefund() throws Exception {
        String token = adminToken();
        createApp(token, APP_ID);
        Long packageId = createPackage(token, APP_ID);
        Long deviceId = registerDevice(APP_ID, DEVICE_CODE);
        JsonNode order = createOrder(APP_ID, deviceId, packageId);
        Long orderId = findOrderId(APP_ID, text(order, "/data/orderNo"));
        assertFalse(text(getOk("/admin/orders/" + orderId, token), "/data/expireAt").isEmpty());

        postOk("/admin/orders/" + orderId + "/close", token, "{\"reason\":\"用户取消支付\"}");
        JsonNode closed = getOk("/admin/orders/" + orderId, token);
        assertEquals("CANCELLED", text(closed, "/data/payStatus"));
        assertEquals("用户取消支付", text(closed, "/data/closeReason"));
        assertFalse(text(closed, "/data/closedAt").isEmpty());

        postCode("/admin/orders/" + orderId + "/mark-paid", token,
                "{\"tradeNo\":\"LATE-MANUAL-TRADE\"}", 409);
        postCode("/api/payment/notify/AGGREGATE", null,
                "{\"payload\":\"verified=true&orderNo=" + text(order, "/data/orderNo")
                        + "&tradeNo=LATE-CALLBACK&status=SUCCESS\"}", 409);
        postCode("/admin/refunds", token,
                "{\"orderId\":" + orderId + ",\"amountCents\":100,\"reason\":\"closed order refund\"}", 409);

        JsonNode eventLogs = getOk("/admin/logs/payment-events", token,
                "appId", APP_ID, "orderId", String.valueOf(orderId), "size", "20");
        assertTrue(eventLogs.toString().contains("ORDER_CLOSED"));
    }

    @Test
    public void expiredPendingOrdersCanBeClosedInBatch() throws Exception {
        String token = adminToken();
        createApp(token, EXPIRED_APP_ID);
        Long packageId = createPackage(token, EXPIRED_APP_ID);
        Long deviceId = registerDevice(EXPIRED_APP_ID, EXPIRED_DEVICE_CODE);

        PaymentOrder expired = paymentOrderRepository.save(new PaymentOrder(
                EXPIRED_APP_ID,
                null,
                deviceId,
                packageId,
                "LIFECYCLE-EXPIRED-001",
                990,
                PayChannel.OTHER,
                "other",
                LocalDateTime.now().minusMinutes(5)
        ));

        int closedCount = paymentService.closeExpiredOrders();
        assertEquals(1, closedCount);
        PaymentOrder closed = paymentOrderRepository.findById(expired.getId()).get();
        assertEquals(PayStatus.CANCELLED, closed.getPayStatus());
        assertEquals("订单超时自动关闭", closed.getCloseReason());
    }

    private String adminToken() throws Exception {
        JsonNode response = postOk("/admin/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"admin123456\"}");
        return text(response, "/data/token");
    }

    private void createApp(String token, String appId) throws Exception {
        postOk("/admin/apps", token,
                "{\"appId\":\"" + appId + "\",\"appName\":\"Lifecycle Device App\",\"appType\":\"DEVICE_ONLY\","
                        + "\"needMobileLogin\":false,\"needDeviceVip\":true}");
    }

    private Long createPackage(String token, String appId) throws Exception {
        JsonNode response = postOk("/admin/packages", token,
                "{\"appId\":\"" + appId + "\",\"packageName\":\"Lifecycle Month\",\"packageType\":\"MEMBERSHIP\","
                        + "\"priceCents\":990,\"durationDays\":30,\"benefitsText\":\"lifecycle\"}");
        return longValue(response, "/data/id");
    }

    private Long registerDevice(String appId, String deviceCode) throws Exception {
        JsonNode response = postOk("/api/device/register", null,
                "{\"appId\":\"" + appId + "\",\"deviceCode\":\"" + deviceCode + "\",\"deviceName\":\"Lifecycle Device\"}");
        return longValue(response, "/data/id");
    }

    private JsonNode createOrder(String appId, Long deviceId, Long packageId) throws Exception {
        return postOk("/api/payment/create-order", null,
                "{\"appId\":\"" + appId + "\",\"deviceId\":" + deviceId
                        + ",\"packageId\":" + packageId + ",\"payChannel\":\"AGGREGATE\"}");
    }

    private Long findOrderId(String appId, String orderNo) throws Exception {
        JsonNode orders = getOk("/admin/orders", adminToken(), "appId", appId, "size", "100");
        for (JsonNode item : orders.path("data").path("content")) {
            if (orderNo.equals(item.path("orderNo").asText())) {
                return item.path("id").asLong();
            }
        }
        throw new AssertionError("Order not found: " + orderNo);
    }

    private JsonNode getOk(String path, String token, String... params) throws Exception {
        MockHttpServletRequestBuilder builder = get(path);
        for (int i = 0; i < params.length; i += 2) {
            builder.param(params[i], params[i + 1]);
        }
        return apiOk(requestJson(builder, token, null, 200));
    }

    private JsonNode postOk(String path, String token, String json) throws Exception {
        return apiOk(requestJson(post(path), token, json, 200));
    }

    private JsonNode postCode(String path, String token, String json, int code) throws Exception {
        JsonNode response = requestJson(post(path), token, json, 200);
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
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
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
