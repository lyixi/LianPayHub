package com.lianpayhub.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.sms.SmsGateway;
import com.lianpayhub.service.notification.sms.SmsSendPayload;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationSendService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSendService.class);

    private final NotificationChannelConfigService configService;
    private final ObjectMapper objectMapper;
    private final List<SmsGateway> smsGateways;
    private final SmsSendLogService smsSendLogService;

    public NotificationSendService(NotificationChannelConfigService configService, ObjectMapper objectMapper,
                                   List<SmsGateway> smsGateways, SmsSendLogService smsSendLogService) {
        this.configService = configService;
        this.objectMapper = objectMapper;
        this.smsGateways = smsGateways;
        this.smsSendLogService = smsSendLogService;
    }

    public NotificationSendResult sendSms(Long configId, String appId, String mobile,
                                          String templateCode, String paramsJson) {
        String safeMobile = requireText(mobile, "mobile 不能为空");
        NotificationChannelConfig config = resolveConfig(configId, NotificationChannelType.SMS, null, true);
        if (config == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置并启用短信通道");
        }
        NotificationSendResult result = sendSmsInternal(config, new SmsSendPayload(appId, safeMobile, null, templateCode, paramsJson), null);
        smsSendLogService.save(config.getId(), config.getProviderCode(), appId, safeMobile, templateCode, paramsJson, result);
        return result;
    }

    public NotificationSendResult sendSmsCode(Long configId, String appId, String mobile, String code) {
        String safeCode = requireText(code, "code 不能为空");
        String paramsJson = smsCodeParams(safeCode, 5);
        return sendSms(configId, appId, mobile, null, paramsJson);
    }

    public NotificationSendResult sendSmsCode(String appId, String mobile, String code, int expireMinutes,
                                              String preferredProvider) {
        String content = "验证码 " + code + "，" + expireMinutes + " 分钟内有效";
        NotificationChannelConfig config = resolveConfig(null, NotificationChannelType.SMS, preferredProvider, false);
        if (config == null && configService.hasAny(NotificationChannelType.SMS)) {
            throw new BusinessException(ErrorCode.CONFLICT, "短信通道未启用");
        }
        return sendSmsInternal(config, new SmsSendPayload(
                appId, mobile, content, null, smsCodeParams(code, expireMinutes)),
                config == null ? "local" : preferredProvider);
    }

    public NotificationSendResult sendEmail(Long configId, String to, String subject, String content, boolean html) {
        requireText(to, "to 不能为空");
        requireText(subject, "subject 不能为空");
        requireText(content, "content 不能为空");
        NotificationChannelConfig config = resolveConfig(configId, NotificationChannelType.EMAIL, null, true);
        if (config == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置并启用邮件通道");
        }
        String provider = normalizeProvider(config.getProviderCode());
        if (isSmtp(config)) {
            return sendSmtp(config, to.trim(), subject.trim(), content, html);
        }
        String messageId = "mail-log-" + UUID.randomUUID().toString();
        log.info("{} email placeholder sent, to={}, subject={}, messageId={}",
                provider, maskEmail(to), subject, messageId);
        return new NotificationSendResult(true, NotificationChannelType.EMAIL, provider, messageId,
                "邮件已写入日志型发送器，真实 SDK 接入后会改为外部投递");
    }

    private NotificationSendResult sendSmsInternal(NotificationChannelConfig config, SmsSendPayload payload,
                                                   String fallbackProvider) {
        String provider = config == null ? normalizeProvider(fallbackProvider) : normalizeProvider(config.getProviderCode());
        if (provider == null) {
            provider = "local";
        }
        NotificationChannelConfig safeConfig = config == null
                ? new NotificationChannelConfig(NotificationChannelType.SMS, provider, "本地短信", null, null,
                null, null, null, null, null, null, null, null, null, null)
                : config;
        SmsGateway gateway = smsGateway(provider);
        if (gateway == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "短信平台尚未接入: " + provider);
        }
        return gateway.send(safeConfig, payload);
    }

    private SmsGateway smsGateway(String provider) {
        for (SmsGateway gateway : smsGateways) {
            if (gateway.supports(provider)) {
                return gateway;
            }
        }
        return null;
    }

    private String smsCodeParams(String code, int expireMinutes) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("code", code);
        node.put("expireMinutes", String.valueOf(expireMinutes));
        node.put("expire", String.valueOf(expireMinutes));
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException ex) {
            return "{\"code\":\"" + code + "\"}";
        }
    }

    private NotificationSendResult sendSmtp(NotificationChannelConfig config, String to, String subject,
                                            String content, boolean html) {
        JsonNode configJson = parseJson(config.getConfigJson());
        JsonNode credentialJson = parseJson(config.getCredentialJson());
        String host = firstNonBlank(text(configJson, "host"), config.getEndpoint());
        if (host == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "SMTP host 不能为空");
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(intValue(configJson, "port", 25));
        sender.setProtocol(firstNonBlank(text(configJson, "protocol"), "smtp"));
        String username = firstNonBlank(text(credentialJson, "username"), text(configJson, "username"));
        String password = firstNonBlank(text(credentialJson, "password"), text(configJson, "password"));
        if (username != null) {
            sender.setUsername(username);
        }
        if (password != null) {
            sender.setPassword(password);
        }
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(booleanValue(configJson, "smtpAuth", username != null)));
        properties.put("mail.smtp.starttls.enable", String.valueOf(booleanValue(configJson, "startTls", false)));
        properties.put("mail.smtp.ssl.enable", String.valueOf(booleanValue(configJson, "ssl", false)));
        String timeout = String.valueOf(intValue(configJson, "timeoutMillis", 10000));
        properties.put("mail.smtp.connectiontimeout", timeout);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.writetimeout", timeout);

        String from = firstNonBlank(config.getSenderAddress(), text(configJson, "from"), username);
        if (from == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "发件邮箱不能为空");
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            if (config.getSenderName() == null) {
                helper.setFrom(from);
            } else {
                helper.setFrom(from, config.getSenderName());
            }
            helper.setTo(to);
            String replyTo = text(configJson, "replyTo");
            if (replyTo != null) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(subject);
            helper.setText(content, html);
            sender.send(message);
            String messageId = "smtp-" + UUID.randomUUID().toString();
            log.info("smtp email sent, provider={}, to={}, subject={}, messageId={}",
                    config.getProviderCode(), maskEmail(to), subject, messageId);
            return new NotificationSendResult(true, NotificationChannelType.EMAIL,
                    config.getProviderCode(), messageId, "邮件已通过 SMTP 发送");
        } catch (MessagingException | UnsupportedEncodingException | MailException ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "邮件发送失败: " + ex.getMessage());
        }
    }

    private boolean isSmtp(NotificationChannelConfig config) {
        String provider = normalizeProvider(config.getProviderCode());
        JsonNode configJson = parseJson(config.getConfigJson());
        return "smtp".equals(provider)
                || "smtp-mail".equals(provider)
                || text(configJson, "host") != null
                || config.getEndpoint() != null && config.getEndpoint().toLowerCase().contains("smtp");
    }

    private NotificationChannelConfig resolveConfig(Long configId, NotificationChannelType type,
                                                    String preferredProvider, boolean requireEnabled) {
        if (configId != null) {
            NotificationChannelConfig config = configService.detail(configId);
            if (config.getChannelType() != type) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "通知通道类型不匹配");
            }
            if (requireEnabled && !config.isEnabled()) {
                throw new BusinessException(ErrorCode.CONFLICT, "通知通道已停用");
            }
            return config;
        }
        return configService.findEnabled(type, preferredProvider).orElse(null);
    }

    private JsonNode parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node == null || !node.isObject() ? objectMapper.createObjectNode() : node;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置 JSON 格式不正确");
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText();
        return normalize(value);
    }

    private int intValue(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asInt(defaultValue);
    }

    private boolean booleanValue(JsonNode node, String field, boolean defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asBoolean(defaultValue);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String safeValue = normalize(value);
            if (safeValue != null) {
                return safeValue;
            }
        }
        return null;
    }

    private String requireText(String value, String message) {
        String safeValue = normalize(value);
        if (safeValue == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return safeValue;
    }

    private String normalizeProvider(String value) {
        String safeValue = normalize(value);
        return safeValue == null ? null : safeValue.toLowerCase();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskEmail(String email) {
        if (email == null) {
            return "******";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "******";
        }
        return email.substring(0, 1) + "****" + email.substring(at);
    }
}
