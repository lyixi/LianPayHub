package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.notification.NotificationChannelStatus;
import com.lianpayhub.domain.notification.NotificationChannelType;
import com.lianpayhub.service.notification.NotificationChannelConfigService;
import com.lianpayhub.domain.notification.SmsSendLog;
import com.lianpayhub.service.notification.NotificationSendResult;
import com.lianpayhub.service.notification.NotificationSendService;
import com.lianpayhub.service.notification.SmsSendLogService;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notification-configs")
public class AdminNotificationChannelConfigController {

    private final NotificationChannelConfigService configService;
    private final NotificationSendService sendService;
    private final SmsSendLogService smsSendLogService;

    public AdminNotificationChannelConfigController(NotificationChannelConfigService configService,
                                                    NotificationSendService sendService,
                                                    SmsSendLogService smsSendLogService) {
        this.configService = configService;
        this.sendService = sendService;
        this.smsSendLogService = smsSendLogService;
    }

    @GetMapping
    public ApiResponse<Page<NotificationChannelConfig>> list(@RequestParam(required = false) NotificationChannelType channelType,
                                                             @RequestParam(required = false) String providerCode,
                                                             @RequestParam(required = false) NotificationChannelStatus status,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(configService.search(channelType, providerCode, status, pageRequest(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(toDetailResponse(configService.detail(id)));
    }

    private Map<String, Object> toDetailResponse(NotificationChannelConfig item) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("id", item.getId());
        data.put("channelType", item.getChannelType());
        data.put("providerCode", item.getProviderCode());
        data.put("displayName", item.getDisplayName());
        data.put("senderName", item.getSenderName());
        data.put("senderAddress", item.getSenderAddress());
        data.put("endpoint", item.getEndpoint());
        data.put("templateCode", item.getTemplateCode());
        data.put("accessKeyId", item.getAccessKeyId());
        data.put("accessKeySecret", item.getAccessKeySecret());
        data.put("secretId", item.getSecretId());
        data.put("secretKey", item.getSecretKey());
        data.put("sdkAppId", item.getSdkAppId());
        data.put("region", item.getRegion());
        data.put("configJson", item.getConfigJson());
        data.put("credentialConfigured", item.isCredentialConfigured());
        data.put("status", item.getStatus());
        data.put("createdAt", item.getCreatedAt());
        data.put("updatedAt", item.getUpdatedAt());
        return data;
    }

    @PostMapping
    public ApiResponse<NotificationChannelConfig> create(@Valid @RequestBody CreateNotificationChannelConfigRequest request) {
        return ApiResponse.ok(configService.create(
                request.channelType(),
                request.providerCode(),
                request.displayName(),
                request.senderName(),
                request.senderAddress(),
                request.endpoint(),
                request.templateCode(),
                request.accessKeyId(),
                request.accessKeySecret(),
                request.secretId(),
                request.secretKey(),
                request.sdkAppId(),
                request.region(),
                request.configJson(),
                request.credentialJson()
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<NotificationChannelConfig> update(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateNotificationChannelConfigRequest request) {
        return ApiResponse.ok(configService.update(
                id,
                request.providerCode(),
                request.displayName(),
                request.senderName(),
                request.senderAddress(),
                request.endpoint(),
                request.templateCode(),
                request.accessKeyId(),
                request.accessKeySecret(),
                request.secretId(),
                request.secretKey(),
                request.sdkAppId(),
                request.region(),
                request.configJson(),
                request.credentialJson()
        ));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<NotificationChannelConfig> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeNotificationChannelConfigStatusRequest request) {
        return ApiResponse.ok(configService.changeStatus(id, request.status()));
    }

    @PostMapping("/sms/send")
    public ApiResponse<NotificationSendResult> sendSms(@Valid @RequestBody SendSmsMessageRequest request) {
        return ApiResponse.ok(sendService.sendSms(
                request.configId(),
                request.appId(),
                request.mobile(),
                request.templateCode(),
                request.paramsJson()
        ));
    }

    @PostMapping("/sms/send-code")
    public ApiResponse<NotificationSendResult> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        if (Boolean.TRUE.equals(request.realSend())) {
            return ApiResponse.ok(sendService.sendSmsCode(
                    request.configId(),
                    request.appId(),
                    request.mobile(),
                    request.code()
            ));
        }
        return ApiResponse.ok(sendService.sendSmsCode(
                request.appId(),
                request.mobile(),
                request.code(),
                5,
                "local"
        ));
    }

    @GetMapping("/sms/logs")
    public ApiResponse<Page<SmsSendLog>> smsLogs(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(smsSendLogService.pageSmsLogs(pageRequest(page, size)));
    }

    @PostMapping("/email/send")
    public ApiResponse<NotificationSendResult> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return ApiResponse.ok(sendService.sendEmail(
                request.configId(),
                request.to(),
                request.subject(),
                request.content(),
                Boolean.TRUE.equals(request.html())
        ));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
