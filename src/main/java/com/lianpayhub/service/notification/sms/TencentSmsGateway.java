package com.lianpayhub.service.notification.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.NotificationSendResult;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TencentSmsGateway extends AbstractSmsGateway {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsGateway.class);

    public TencentSmsGateway(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(String providerCode) {
        return "tencent".equals(providerCode) || "tencent-sms".equals(providerCode);
    }

    @Override
    public NotificationSendResult send(NotificationChannelConfig config, SmsSendPayload payload) {
        JsonNode configJson = parseJson(config.getConfigJson());
        JsonNode credentialJson = parseJson(config.getCredentialJson());
        String secretId = requireText(firstNonBlank(config.getSecretId(), text(credentialJson, "secretId")),
                "腾讯云短信 secretId 不能为空");
        String secretKey = requireText(firstNonBlank(config.getSecretKey(), text(credentialJson, "secretKey")),
                "腾讯云短信 secretKey 不能为空");
        String smsSdkAppId = requireText(firstNonBlank(config.getSdkAppId(), text(configJson, "smsSdkAppId"), text(configJson, "sdkAppId")),
                "腾讯云短信 smsSdkAppId 不能为空");
        String signName = requireText(firstNonBlank(config.getSenderName(), config.getSenderAddress(),
                text(configJson, "signName")), "腾讯云短信签名 signName 不能为空");
        String templateId = requireText(firstNonBlank(payload.getTemplateCode(), config.getTemplateCode(), text(configJson, "templateId")),
                "腾讯云短信 templateId 不能为空");
        String endpoint = firstNonBlank(config.getEndpoint(), text(configJson, "endpoint"), "sms.tencentcloudapi.com");
        String region = firstNonBlank(config.getRegion(), text(configJson, "region"), "ap-guangzhou");
        try {
            Credential credential = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(endpoint);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(credential, region, clientProfile);

            SendSmsRequest request = new SendSmsRequest();
            request.setSmsSdkAppId(smsSdkAppId);
            request.setSignName(signName);
            request.setTemplateId(templateId);
            request.setPhoneNumberSet(new String[]{payload.getMobile()});
            String[] params = templateParams(payload.getParamsJson(), configJson);
            if (params.length > 0) {
                request.setTemplateParamSet(params);
            }

            SendSmsResponse response = client.SendSms(request);
            SendStatus status = response.getSendStatusSet() == null || response.getSendStatusSet().length == 0
                    ? null : response.getSendStatusSet()[0];
            String code = status == null ? null : status.getCode();
            String message = status == null ? null : status.getMessage();
            String serialNo = status == null ? response.getRequestId() : firstNonBlank(status.getSerialNo(), response.getRequestId());
            if (!"Ok".equalsIgnoreCase(code)) {
                throw new BusinessException(ErrorCode.SERVER_ERROR,
                        "腾讯云短信发送失败: " + firstNonBlank(message, code, "未知错误"));
            }
            log.info("tencent sms sent, appId={}, mobile={}, templateId={}, serialNo={}",
                    payload.getAppId(), maskMobile(payload.getMobile()), templateId, serialNo);
            return new NotificationSendResult(true, NotificationChannelType.SMS, "tencent", serialNo,
                    "腾讯云短信已提交");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "腾讯云短信发送异常: " + ex.getMessage());
        }
    }

    private String[] templateParams(String paramsJson, JsonNode configJson) {
        String json = firstNonBlank(paramsJson, text(configJson, "templateParamJson"));
        if (json == null) {
            return new String[0];
        }
        JsonNode node = parseJson(json);
        if (node.has("templateParamSet") && node.get("templateParamSet").isArray()) {
            return stringArray(node.get("templateParamSet"));
        }
        if (node.has("values") && node.get("values").isArray()) {
            return stringArray(node.get("values"));
        }
        if (configJson.has("templateParamKeys") && configJson.get("templateParamKeys").isArray()) {
            List<String> values = new ArrayList<String>();
            for (JsonNode key : configJson.get("templateParamKeys")) {
                String field = key.asText();
                if (node.has(field)) {
                    values.add(node.get(field).asText());
                }
            }
            return values.toArray(new String[0]);
        }
        List<String> values = new ArrayList<String>();
        if (node.has("code")) {
            values.add(node.get("code").asText());
        }
        return values.toArray(new String[0]);
    }

    private String[] stringArray(JsonNode array) {
        List<String> values = new ArrayList<String>();
        for (JsonNode item : array) {
            values.add(item.asText());
        }
        return values.toArray(new String[0]);
    }
}
