package com.lianpayhub.service.payment.provider;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PayMode;
import com.lianpayhub.service.payment.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AlipayPaymentProvider implements PaymentProvider {

    private final AlipayConfigFactory configFactory;
    private final ObjectMapper objectMapper;

    public AlipayPaymentProvider(AlipayConfigFactory configFactory, ObjectMapper objectMapper) {
        this.configFactory = configFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerCode() {
        return "alipay";
    }

    @Override
    public PayChannel payChannel() {
        return PayChannel.ALIPAY;
    }

    @Override
    public PaymentCreateResult createPayment(PaymentCreateContext context) {
        if (context.channelConfig() == null) {
            return fallbackPayment(context);
        }
        AlipayConfig config = configFactory.create(context.channelConfig(), context.returnUrl());
        PayMode mode = context.payMode() == null ? payMode(config.defaultPayMode()) : context.payMode();
        switch (mode) {
            case PAGE:
                return createPagePayment(context, config);
            case APP:
                return createAppPayment(context, config);
            case QR:
            default:
                return createQrPayment(context, config);
        }
    }

    @Override
    public PaymentCallbackResult parseCallback(String rawPayload) {
        return parseCallbackFromParams(parsePayload(rawPayload), rawPayload);
    }

    private PaymentCallbackResult parseCallbackFromParams(Map<String, String> params, String rawPayload) {
        String orderNo = firstText(params.get("out_trade_no"), params.get("orderNo"), params.get("order_no"));
        String tradeNo = firstText(params.get("trade_no"), params.get("tradeNo"));
        String tradeStatus = params.get("trade_status");
        boolean success = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        String publicKey = firstText(params.get("alipayPublicKey"), params.get("publicKey"));
        boolean verified = success && (publicKey == null || verify(params, publicKey));
        return new PaymentCallbackResult(verified, orderNo, tradeNo, tradeNo, rawPayload);
    }

    @Override
    public PaymentCallbackResult parseCallback(PaymentChannelContext context, String rawPayload) {
        Map<String, String> params = parsePayload(rawPayload);
        AlipayConfig config = configFactory.create(context.channelConfig(), null);
        params.put("alipayPublicKey", config.alipayPublicKey());
        return parseCallbackFromParams(params, rawPayload);
    }

    @Override
    public ChannelOrderResult queryOrder(PaymentChannelContext context, String orderNo, String tradeNo) {
        AlipayConfig config = configFactory.create(context.channelConfig(), null);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);
        model.setTradeNo(tradeNo);
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = client(config).execute(request);
            Map<String, Object> raw = responseMap(response.getBody());
            return new ChannelOrderResult(true, response.isSuccess(), response.getTradeNo(), response.getTradeStatus(), response.getSubMsg(), raw);
        } catch (AlipayApiException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝查单失败: " + ex.getErrMsg());
        }
    }

    @Override
    public ChannelRefundResult refund(PaymentChannelContext context, String orderNo, String tradeNo,
                                      String refundNo, Integer amountCents, String reason) {
        AlipayConfig config = configFactory.create(context.channelConfig(), null);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(orderNo);
        model.setTradeNo(tradeNo);
        model.setOutRequestNo(refundNo);
        model.setRefundAmount(amount(amountCents));
        model.setRefundReason(reason);
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = client(config).execute(request);
            Map<String, Object> raw = responseMap(response.getBody());
            return new ChannelRefundResult(true, response.isSuccess(), response.getTradeNo(), response.getSubMsg(), raw);
        } catch (AlipayApiException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝退款失败: " + ex.getErrMsg());
        }
    }

    private PaymentCreateResult createQrPayment(PaymentCreateContext context, AlipayConfig config) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(config.notifyUrl());
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(context.orderNo());
        model.setTotalAmount(amount(context.amountCents()));
        model.setSubject(context.subject());
        request.setBizModel(model);
        try {
            AlipayTradePrecreateResponse response = client(config).execute(request);
            if (!response.isSuccess()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝预下单失败: " + response.getSubMsg());
            }
            Map<String, Object> params = baseParams(context, PayMode.QR);
            params.put("qrCode", response.getQrCode());
            params.put("payUrl", response.getQrCode());
            params.put("raw", responseMap(response.getBody()));
            return new PaymentCreateResult(context.orderNo(), providerCode(), params);
        } catch (AlipayApiException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝预下单失败: " + ex.getErrMsg());
        }
    }

    private PaymentCreateResult createPagePayment(PaymentCreateContext context, AlipayConfig config) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(config.notifyUrl());
        request.setReturnUrl(config.returnUrl());
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(context.orderNo());
        model.setTotalAmount(amount(context.amountCents()));
        model.setSubject(context.subject());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);
        try {
            AlipayTradePagePayResponse response = client(config).pageExecute(request, "GET");
            Map<String, Object> params = baseParams(context, PayMode.PAGE);
            params.put("payUrl", response.getBody());
            params.put("browserUrl", response.getBody());
            return new PaymentCreateResult(context.orderNo(), providerCode(), params);
        } catch (AlipayApiException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝网页支付下单失败: " + ex.getErrMsg());
        }
    }

    private PaymentCreateResult createAppPayment(PaymentCreateContext context, AlipayConfig config) {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(config.notifyUrl());
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(context.orderNo());
        model.setTotalAmount(amount(context.amountCents()));
        model.setSubject(context.subject());
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        try {
            AlipayTradeAppPayResponse response = client(config).sdkExecute(request);
            Map<String, Object> params = baseParams(context, PayMode.APP);
            params.put("orderString", response.getBody());
            return new PaymentCreateResult(context.orderNo(), providerCode(), params);
        } catch (AlipayApiException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付宝 App 支付下单失败: " + ex.getErrMsg());
        }
    }

    private PaymentCreateResult fallbackPayment(PaymentCreateContext context) {
        Map<String, Object> params = baseParams(context, context.payMode() == null ? PayMode.QR : context.payMode());
        params.put("configured", false);
        return new PaymentCreateResult(context.orderNo(), providerCode(), params);
    }

    private AlipayClient client(AlipayConfig config) {
        return new DefaultAlipayClient(
                config.gatewayUrl(),
                config.appId(),
                config.merchantPrivateKey(),
                "json",
                config.charset(),
                config.alipayPublicKey(),
                config.signType()
        );
    }

    private boolean verify(Map<String, String> params, String publicKey) {
        try {
            Map<String, String> copy = new HashMap<>(params);
            copy.remove("alipayPublicKey");
            copy.remove("publicKey");
            return AlipaySignature.rsaCheckV1(copy, publicKey, "UTF-8", firstText(params.get("sign_type"), "RSA2"));
        } catch (AlipayApiException ex) {
            return false;
        }
    }

    private Map<String, String> parsePayload(String rawPayload) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawPayload == null || rawPayload.trim().isEmpty()) {
            return params;
        }
        try {
            Map<?, ?> map = objectMapper.readValue(rawPayload, Map.class);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() != null) params.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            return params;
        } catch (Exception ignored) {
        }
        for (String pair : rawPayload.split("&")) {
            int index = pair.indexOf('=');
            if (index <= 0) continue;
            String key = urlDecode(pair.substring(0, index));
            String value = urlDecode(pair.substring(index + 1));
            params.put(key, value);
        }
        return params;
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return value;
        }
    }

    private Map<String, Object> responseMap(String body) {
        if (body == null || body.trim().isEmpty()) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(body, Map.class);
        } catch (Exception ex) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("body", body);
            return map;
        }
    }

    private Map<String, Object> baseParams(PaymentCreateContext context, PayMode mode) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("orderNo", context.orderNo());
        params.put("amountCents", context.amountCents());
        params.put("subject", context.subject());
        params.put("provider", providerCode());
        params.put("payMode", mode.name());
        params.put("configured", true);
        return params;
    }

    private String amount(Integer cents) {
        return BigDecimal.valueOf(cents == null ? 0 : cents).divide(BigDecimal.valueOf(100)).toPlainString();
    }

    private PayMode payMode(String value) {
        try {
            return PayMode.valueOf(firstText(value, "PAGE"));
        } catch (Exception ex) {
            return PayMode.PAGE;
        }
    }

    private String firstText(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return null;
    }
}
