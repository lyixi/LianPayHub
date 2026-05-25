package com.lianpayhub.web.admin;

import com.lianpayhub.domain.adapter.AdapterReport;
import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.log.AdminOperationLog;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.log.PaymentEventLog;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PaymentCallbackLog;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import com.lianpayhub.domain.payment.PaymentChannelConfigStatus;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.AdapterReportRepository;
import com.lianpayhub.repository.AdminOperationLogRepository;
import com.lianpayhub.repository.AdminUserRepository;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.AppLoginLogRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.LaunchRecordRepository;
import com.lianpayhub.repository.MemberInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.repository.PaymentCallbackLogRepository;
import com.lianpayhub.repository.PaymentChannelConfigRepository;
import com.lianpayhub.repository.PaymentEventLogRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.repository.PaymentRefundRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/exports")
public class AdminExportController {

    private final AppInfoRepository appInfoRepository;
    private final PaymentChannelConfigRepository paymentChannelConfigRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final PaymentRefundRepository refundRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AdapterReportRepository adapterReportRepository;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final PaymentEventLogRepository paymentEventLogRepository;
    private final AdminUserRepository adminUserRepository;

    public AdminExportController(AppInfoRepository appInfoRepository,
                                 PaymentChannelConfigRepository paymentChannelConfigRepository,
                                 PackageInfoRepository packageInfoRepository,
                                 UserInfoRepository userInfoRepository,
                                 UserAppBindingRepository userAppBindingRepository,
                                 DeviceInfoRepository deviceInfoRepository,
                                 MemberInfoRepository memberInfoRepository,
                                 PaymentOrderRepository paymentOrderRepository,
                                 PaymentCallbackLogRepository callbackLogRepository,
                                 PaymentRefundRepository refundRepository,
                                 LaunchRecordRepository launchRecordRepository,
                                 AdapterReportRepository adapterReportRepository,
                                 AdminOperationLogRepository adminOperationLogRepository,
                                 AppLoginLogRepository appLoginLogRepository,
                                 PaymentEventLogRepository paymentEventLogRepository,
                                 AdminUserRepository adminUserRepository) {
        this.appInfoRepository = appInfoRepository;
        this.paymentChannelConfigRepository = paymentChannelConfigRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.userInfoRepository = userInfoRepository;
        this.userAppBindingRepository = userAppBindingRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.refundRepository = refundRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.adapterReportRepository = adapterReportRepository;
        this.adminOperationLogRepository = adminOperationLogRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.paymentEventLogRepository = paymentEventLogRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @GetMapping(value = "/apps", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> apps(@RequestParam(defaultValue = "1000") int limit) {
        List<AppInfo> rows = appInfoRepository.findAll(exportPage(limit)).getContent();
        return csv("apps.csv",
                Arrays.asList("id", "appId", "appName", "appType", "needMobileLogin", "needDeviceVip",
                        "status", "appSecretVersion", "createdAt", "updatedAt"),
                rows,
                new RowMapper<AppInfo>() {
                    @Override
                    public List<Object> map(AppInfo item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getAppName(), item.getAppType(),
                                item.isNeedMobileLogin(), item.isNeedDeviceVip(), item.getStatus(),
                                item.getAppSecretVersion(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/payment-configs", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> paymentConfigs(@RequestParam(required = false) String appId,
                                                 @RequestParam(required = false) String payChannel,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1000") int limit) {
        List<PaymentChannelConfig> rows = paymentChannelConfigRepository.search(
                blankToNull(appId),
                enumOrNull(PayChannel.class, payChannel),
                enumOrNull(PaymentChannelConfigStatus.class, status),
                exportPage(limit)).getContent();
        return csv("payment-configs.csv",
                Arrays.asList("id", "appId", "payChannel", "providerCode", "merchantId", "channelAppId",
                        "notifyUrl", "configJson", "credentialConfigured", "status", "createdAt", "updatedAt"),
                rows,
                new RowMapper<PaymentChannelConfig>() {
                    @Override
                    public List<Object> map(PaymentChannelConfig item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getPayChannel(),
                                item.getProviderCode(), item.getMerchantId(), item.getChannelAppId(),
                                item.getNotifyUrl(), item.getConfigJson(), item.isCredentialConfigured(),
                                item.getStatus(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/packages", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> packages(@RequestParam(required = false) String appId,
                                           @RequestParam(defaultValue = "1000") int limit) {
        List<PackageInfo> source = blankToNull(appId) == null
                ? packageInfoRepository.findAll(exportPage(limit)).getContent()
                : packageInfoRepository.findByAppId(appId);
        return csv("packages.csv",
                Arrays.asList("id", "appId", "packageName", "packageType", "priceCents", "durationDays",
                        "status", "benefitsText", "createdAt", "updatedAt"),
                limitRows(source, limit),
                new RowMapper<PackageInfo>() {
                    @Override
                    public List<Object> map(PackageInfo item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getPackageName(),
                                item.getPackageType(), item.getPriceCents(), item.getDurationDays(),
                                item.getStatus(), item.getBenefitsText(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/users", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> users(@RequestParam(required = false) String mobile,
                                        @RequestParam(defaultValue = "1000") int limit) {
        List<UserInfo> rows = blankToNull(mobile) == null
                ? userInfoRepository.findAll(exportPage(limit)).getContent()
                : userInfoRepository.findByMobile(mobile, exportPage(limit)).getContent();
        return csv("users.csv",
                Arrays.asList("id", "mobile", "userType", "openId", "status", "createdAt", "updatedAt"),
                rows,
                new RowMapper<UserInfo>() {
                    @Override
                    public List<Object> map(UserInfo item) {
                        return Arrays.asList(item.getId(), item.getMobile(), item.getUserType(), item.getOpenId(),
                                item.getStatus(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/user-bindings", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> userBindings(@RequestParam(required = false) String appId,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1000") int limit) {
        List<UserAppBinding> rows = userAppBindingRepository.search(
                blankToNull(appId), userId, enumOrNull(BindingStatus.class, status), exportPage(limit)).getContent();
        return csv("user-bindings.csv",
                Arrays.asList("id", "userId", "appId", "bindType", "status", "createdAt", "updatedAt"),
                rows,
                new RowMapper<UserAppBinding>() {
                    @Override
                    public List<Object> map(UserAppBinding item) {
                        return Arrays.asList(item.getId(), item.getUserId(), item.getAppId(), item.getBindType(),
                                item.getStatus(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/devices", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> devices(@RequestParam(required = false) String appId,
                                          @RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) String deviceCode,
                                          @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<DeviceInfo> rows;
        if (blankToNull(appId) == null) {
            rows = deviceInfoRepository.findAll(pageRequest).getContent();
        } else if (blankToNull(deviceCode) != null) {
            rows = deviceInfoRepository.findByAppIdAndDeviceCode(appId, deviceCode, pageRequest).getContent();
        } else if (userId != null) {
            rows = deviceInfoRepository.findByAppIdAndUserId(appId, userId, pageRequest).getContent();
        } else {
            rows = deviceInfoRepository.findByAppId(appId, pageRequest).getContent();
        }
        return csv("devices.csv",
                Arrays.asList("id", "appId", "userId", "deviceCode", "deviceName", "deviceType",
                        "deviceFingerprint", "bindStatus", "bindAt", "lastLaunchAt", "createdAt", "updatedAt"),
                rows,
                new RowMapper<DeviceInfo>() {
                    @Override
                    public List<Object> map(DeviceInfo item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getUserId(), item.getDeviceCode(),
                                item.getDeviceName(), item.getDeviceType(), item.getDeviceFingerprint(),
                                item.getBindStatus(), item.getBindAt(), item.getLastLaunchAt(),
                                item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/members", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> members(@RequestParam(required = false) String appId,
                                          @RequestParam(defaultValue = "1000") int limit) {
        List<MemberInfo> rows = blankToNull(appId) == null
                ? memberInfoRepository.findAll(exportPage(limit)).getContent()
                : memberInfoRepository.findByAppId(appId, exportPage(limit)).getContent();
        return csv("members.csv",
                Arrays.asList("id", "appId", "memberSubjectType", "userId", "deviceId", "packageId",
                        "status", "startAt", "expireAt", "orderId", "createdAt", "updatedAt"),
                rows,
                new RowMapper<MemberInfo>() {
                    @Override
                    public List<Object> map(MemberInfo item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getMemberSubjectType(),
                                item.getUserId(), item.getDeviceId(), item.getPackageId(), item.getStatus(),
                                item.getStartAt(), item.getExpireAt(), item.getOrderId(),
                                item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/orders", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> orders(@RequestParam(required = false) String appId,
                                         @RequestParam(defaultValue = "1000") int limit) {
        List<PaymentOrder> rows = blankToNull(appId) == null
                ? paymentOrderRepository.findAll(exportPage(limit)).getContent()
                : paymentOrderRepository.findByAppId(appId, exportPage(limit)).getContent();
        return csv("orders.csv",
                Arrays.asList("id", "appId", "userId", "deviceId", "packageId", "orderNo", "amountCents",
                        "payChannel", "payProvider", "payStatus", "tradeNo", "channelOrderNo", "callbackCount",
                        "refundedAmountCents", "paidAt", "expireAt", "closedAt", "closeReason",
                        "createdAt", "updatedAt"),
                rows,
                new RowMapper<PaymentOrder>() {
                    @Override
                    public List<Object> map(PaymentOrder item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getUserId(), item.getDeviceId(),
                                item.getPackageId(), item.getOrderNo(), item.getAmountCents(),
                                item.getPayChannel(), item.getPayProvider(), item.getPayStatus(),
                                item.getTradeNo(), item.getChannelOrderNo(), item.getCallbackCount(),
                                item.getRefundedAmountCents(), item.getPaidAt(), item.getExpireAt(),
                                item.getClosedAt(), item.getCloseReason(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/payment-callbacks", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> paymentCallbacks(@RequestParam(required = false) String appId,
                                                   @RequestParam(required = false) Long orderId,
                                                   @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<PaymentCallbackLog> rows = orderId != null
                ? callbackLogRepository.findByOrderId(orderId, pageRequest).getContent()
                : blankToNull(appId) == null
                ? callbackLogRepository.findAll(pageRequest).getContent()
                : callbackLogRepository.findByAppId(appId, pageRequest).getContent();
        return csv("payment-callbacks.csv",
                Arrays.asList("id", "appId", "orderId", "payChannel", "payProvider", "tradeNo",
                        "verifyStatus", "processStatus", "errorMessage", "rawPayload", "createdAt", "updatedAt"),
                rows,
                new RowMapper<PaymentCallbackLog>() {
                    @Override
                    public List<Object> map(PaymentCallbackLog item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getOrderId(), item.getPayChannel(),
                                item.getPayProvider(), item.getTradeNo(), item.getVerifyStatus(),
                                item.getProcessStatus(), item.getErrorMessage(), item.getRawPayload(),
                                item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/payment-refunds", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> paymentRefunds(@RequestParam(required = false) String appId,
                                                 @RequestParam(required = false) Long orderId,
                                                 @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<PaymentRefund> rows = orderId != null
                ? refundRepository.findByOrderId(orderId, pageRequest).getContent()
                : blankToNull(appId) == null
                ? refundRepository.findAll(pageRequest).getContent()
                : refundRepository.findByAppId(appId, pageRequest).getContent();
        return csv("payment-refunds.csv",
                Arrays.asList("id", "appId", "orderId", "refundNo", "amountCents", "reason", "status",
                        "channelRefundNo", "processedAt", "createdAt", "updatedAt"),
                rows,
                new RowMapper<PaymentRefund>() {
                    @Override
                    public List<Object> map(PaymentRefund item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getOrderId(), item.getRefundNo(),
                                item.getAmountCents(), item.getReason(), item.getStatus(),
                                item.getChannelRefundNo(), item.getProcessedAt(),
                                item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/launch-records", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> launchRecords(@RequestParam(required = false) String appId,
                                                @RequestParam(required = false) Long deviceId,
                                                @RequestParam(required = false) Long userId,
                                                @RequestParam(defaultValue = "1000") int limit) {
        List<LaunchRecord> rows = findLaunchRecords(appId, deviceId, userId, exportPage(limit));
        return csv("launch-records.csv",
                Arrays.asList("id", "appId", "deviceId", "userId", "platform", "version",
                        "networkType", "ipAddress", "eventType", "eventData", "createdAt"),
                rows,
                new RowMapper<LaunchRecord>() {
                    @Override
                    public List<Object> map(LaunchRecord item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getDeviceId(), item.getUserId(),
                                item.getPlatform(), item.getVersion(), item.getNetworkType(), item.getIpAddress(),
                                item.getEventType(), item.getEventData(), item.getCreatedAt());
                    }
                });
    }

    @GetMapping(value = "/adapter-reports", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> adapterReports(@RequestParam(required = false) String appId,
                                                 @RequestParam(required = false) String sourceId,
                                                 @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<AdapterReport> rows;
        if (blankToNull(appId) == null) {
            rows = adapterReportRepository.findAll(pageRequest).getContent();
        } else if (blankToNull(sourceId) != null) {
            rows = adapterReportRepository.findByAppIdAndSourceId(appId, sourceId, pageRequest).getContent();
        } else {
            rows = adapterReportRepository.findByAppId(appId, pageRequest).getContent();
        }
        return csv("adapter-reports.csv",
                Arrays.asList("id", "appId", "sourceId", "reportType", "status", "payload",
                        "createdAt", "updatedAt"),
                rows,
                new RowMapper<AdapterReport>() {
                    @Override
                    public List<Object> map(AdapterReport item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getSourceId(),
                                item.getReportType(), item.getStatus(), item.getPayload(),
                                item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    @GetMapping(value = "/logs/admin-operations", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> adminOperationLogs(@RequestParam(required = false) Long adminId,
                                                     @RequestParam(defaultValue = "1000") int limit) {
        List<AdminOperationLog> rows = adminId == null
                ? adminOperationLogRepository.findAll(exportPage(limit)).getContent()
                : adminOperationLogRepository.findByAdminId(adminId, exportPage(limit)).getContent();
        return csv("admin-operation-logs.csv",
                Arrays.asList("id", "adminId", "username", "operationType", "targetType", "targetId",
                        "requestMethod", "requestUri", "ipAddress", "userAgent", "resultStatus",
                        "errorMessage", "requestBody", "createdAt"),
                rows,
                new RowMapper<AdminOperationLog>() {
                    @Override
                    public List<Object> map(AdminOperationLog item) {
                        return Arrays.asList(item.getId(), item.getAdminId(), item.getUsername(),
                                item.getOperationType(), item.getTargetType(), item.getTargetId(),
                                item.getRequestMethod(), item.getRequestUri(), item.getIpAddress(),
                                item.getUserAgent(), item.getResultStatus(), item.getErrorMessage(),
                                item.getRequestBody(), item.getCreatedAt());
                    }
                });
    }

    @GetMapping(value = "/logs/app-logins", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> appLoginLogs(@RequestParam(required = false) String appId,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false) String mobile,
                                               @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<AppLoginLog> rows;
        if (userId != null) {
            rows = appLoginLogRepository.findByUserId(userId, pageRequest).getContent();
        } else if (blankToNull(mobile) != null && blankToNull(appId) != null) {
            rows = appLoginLogRepository.findByAppIdAndMobile(appId, mobile, pageRequest).getContent();
        } else if (blankToNull(mobile) != null) {
            rows = appLoginLogRepository.findByMobile(mobile, pageRequest).getContent();
        } else if (blankToNull(appId) != null) {
            rows = appLoginLogRepository.findByAppId(appId, pageRequest).getContent();
        } else {
            rows = appLoginLogRepository.findAll(pageRequest).getContent();
        }
        return csv("app-login-logs.csv",
                Arrays.asList("id", "appId", "userId", "mobile", "loginType", "deviceId", "deviceCode",
                        "ipAddress", "userAgent", "resultStatus", "errorMessage", "createdAt"),
                rows,
                new RowMapper<AppLoginLog>() {
                    @Override
                    public List<Object> map(AppLoginLog item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getUserId(), item.getMobile(),
                                item.getLoginType(), item.getDeviceId(), item.getDeviceCode(), item.getIpAddress(),
                                item.getUserAgent(), item.getResultStatus(), item.getErrorMessage(),
                                item.getCreatedAt());
                    }
                });
    }

    @GetMapping(value = "/logs/launches", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> launchLogs(@RequestParam(required = false) String appId,
                                             @RequestParam(required = false) Long deviceId,
                                             @RequestParam(required = false) Long userId,
                                             @RequestParam(defaultValue = "1000") int limit) {
        return launchRecords(appId, deviceId, userId, limit);
    }

    @GetMapping(value = "/logs/payment-events", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> paymentEventLogs(@RequestParam(required = false) String appId,
                                                   @RequestParam(required = false) Long orderId,
                                                   @RequestParam(defaultValue = "1000") int limit) {
        PageRequest pageRequest = exportPage(limit);
        List<PaymentEventLog> rows = orderId != null
                ? paymentEventLogRepository.findByOrderId(orderId, pageRequest).getContent()
                : blankToNull(appId) == null
                ? paymentEventLogRepository.findAll(pageRequest).getContent()
                : paymentEventLogRepository.findByAppId(appId, pageRequest).getContent();
        return csv("payment-event-logs.csv",
                Arrays.asList("id", "appId", "orderId", "eventType", "payChannel", "payProvider",
                        "amountCents", "tradeNo", "operatorType", "operatorId", "eventData", "createdAt"),
                rows,
                new RowMapper<PaymentEventLog>() {
                    @Override
                    public List<Object> map(PaymentEventLog item) {
                        return Arrays.asList(item.getId(), item.getAppId(), item.getOrderId(), item.getEventType(),
                                item.getPayChannel(), item.getPayProvider(), item.getAmountCents(),
                                item.getTradeNo(), item.getOperatorType(), item.getOperatorId(),
                                item.getEventData(), item.getCreatedAt());
                    }
                });
    }

    @GetMapping(value = "/admin-users", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> adminUsers(@RequestParam(required = false) String username,
                                             @RequestParam(defaultValue = "1000") int limit) {
        List<AdminUser> rows = blankToNull(username) == null
                ? adminUserRepository.findAll(exportPage(limit)).getContent()
                : adminUserRepository.findByUsernameContaining(username, exportPage(limit)).getContent();
        return csv("admin-users.csv",
                Arrays.asList("id", "username", "displayName", "status", "lastLoginAt", "createdAt", "updatedAt"),
                rows,
                new RowMapper<AdminUser>() {
                    @Override
                    public List<Object> map(AdminUser item) {
                        return Arrays.asList(item.getId(), item.getUsername(), item.getDisplayName(),
                                item.getStatus(), item.getLastLoginAt(), item.getCreatedAt(), item.getUpdatedAt());
                    }
                });
    }

    private List<LaunchRecord> findLaunchRecords(String appId, Long deviceId, Long userId, PageRequest pageRequest) {
        if (blankToNull(appId) == null) {
            return launchRecordRepository.findAll(pageRequest).getContent();
        }
        if (deviceId != null) {
            return launchRecordRepository.findByAppIdAndDeviceId(appId, deviceId, pageRequest).getContent();
        }
        if (userId != null) {
            return launchRecordRepository.findByAppIdAndUserId(appId, userId, pageRequest).getContent();
        }
        return launchRecordRepository.findByAppId(appId, pageRequest).getContent();
    }

    private <T> ResponseEntity<String> csv(String filename, List<String> headers, List<T> source,
                                           RowMapper<T> rowMapper) {
        StringBuilder builder = new StringBuilder();
        appendRow(builder, new ArrayList<Object>(headers));
        for (T item : source) {
            appendRow(builder, rowMapper.map(item));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(builder.toString());
    }

    private void appendRow(StringBuilder builder, List<Object> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(csvValue(values.get(i)));
        }
        builder.append("\r\n");
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (looksLikeSpreadsheetFormula(text)) {
            text = "'" + text;
        }
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private boolean looksLikeSpreadsheetFormula(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        char first = text.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@';
    }

    private PageRequest exportPage(int limit) {
        return PageRequest.of(0, safeLimit(limit), exportSort());
    }

    private Sort exportSort() {
        return Sort.by(Sort.Direction.DESC, "id");
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), 5000);
    }

    private <T> List<T> limitRows(List<T> source, int limit) {
        int end = Math.min(source.size(), safeLimit(limit));
        return new ArrayList<T>(source.subList(0, end));
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private <E extends Enum<E>> E enumOrNull(Class<E> enumClass, String value) {
        String text = blankToNull(value);
        return text == null ? null : Enum.valueOf(enumClass, text);
    }

    private interface RowMapper<T> {
        List<Object> map(T item);
    }
}
