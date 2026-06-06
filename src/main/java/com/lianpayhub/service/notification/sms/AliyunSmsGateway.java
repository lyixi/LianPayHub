package com.lianpayhub.service.notification.sms;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.NotificationSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsGateway extends AbstractSmsGateway {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsGateway.class);

    public AliyunSmsGateway(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(String providerCode) {
        return "aliyun".equals(providerCode) || "aliyun-sms".equals(providerCode);
    }

    @Override
    public NotificationSendResult send(NotificationChannelConfig config, SmsSendPayload payload) {
        JsonNode configJson = parseJson(config.getConfigJson());
        JsonNode credentialJson = parseJson(config.getCredentialJson());
        String accessKeyId = requireText(firstNonBlank(config.getAccessKeyId(), text(credentialJson, "accessKeyId")),
                "阿里云短信 accessKeyId 不能为空");
        String accessKeySecret = requireText(firstNonBlank(config.getAccessKeySecret(), text(credentialJson, "accessKeySecret")),
                "阿里云短信 accessKeySecret 不能为空");
        String signName = requireText(firstNonBlank(config.getSenderName(), config.getSenderAddress(),
                text(configJson, "signName")), "阿里云短信签名 signName 不能为空");
        String templateCode = requireText(firstNonBlank(payload.getTemplateCode(), config.getTemplateCode(), text(configJson, "templateCode")),
                "阿里云短信 templateCode 不能为空");
        String templateParam = firstNonBlank(payload.getParamsJson(), text(configJson, "templateParamJson"), "{}");
        String endpoint = firstNonBlank(config.getEndpoint(), text(configJson, "endpoint"), "dysmsapi.aliyuncs.com");
        try {
            com.aliyun.teaopenapi.models.Config clientConfig = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            clientConfig.endpoint = endpoint;
            com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(clientConfig);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(payload.getMobile())
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);
            SendSmsResponse response = client.sendSms(request);
            String code = response.getBody() == null ? null : response.getBody().getCode();
            String message = response.getBody() == null ? null : response.getBody().getMessage();
            String bizId = response.getBody() == null ? null : response.getBody().getBizId();
            if (!"OK".equalsIgnoreCase(code)) {
                throw new BusinessException(ErrorCode.SERVER_ERROR,
                        "阿里云短信发送失败: " + firstNonBlank(message, code, "未知错误"));
            }
            log.info("aliyun sms sent, appId={}, mobile={}, templateCode={}, bizId={}",
                    payload.getAppId(), maskMobile(payload.getMobile()), templateCode, bizId);
            return new NotificationSendResult(true, NotificationChannelType.SMS, "aliyun", bizId,
                    "阿里云短信已提交");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "阿里云短信发送异常: " + ex.getMessage());
        }
    }
}
