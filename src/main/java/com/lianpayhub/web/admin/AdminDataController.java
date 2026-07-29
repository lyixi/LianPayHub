package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.member.MemberSubjectType;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.payment.PaymentCallbackLog;
import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.domain.user.UserStatus;
import com.lianpayhub.domain.adapter.AdapterReport;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.repository.*;
import com.lianpayhub.service.report.AnalyticsGranularity;
import com.lianpayhub.service.report.AnalyticsMetric;
import com.lianpayhub.service.report.AnalyticsReportService;
import com.lianpayhub.service.report.AnalyticsResult;
import com.lianpayhub.service.report.AdminOverviewResult;
import com.lianpayhub.service.report.AdminOverviewService;
import com.lianpayhub.service.report.AdminTrendService;
import com.lianpayhub.service.report.DailyTrendItem;
import com.lianpayhub.service.report.PaymentSummaryResult;
import com.lianpayhub.service.report.PaymentSummaryService;
import com.lianpayhub.service.admin.AdminAggregateService;
import com.lianpayhub.service.admin.AdminUserProfileResult;
import com.lianpayhub.service.admin.AdminUserProfileService;
import com.lianpayhub.service.member.GrantMemberCommand;
import com.lianpayhub.service.member.MemberService;
import com.lianpayhub.service.device.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminDataController {

    private final UserInfoRepository userInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceCodeChangeLogRepository deviceCodeChangeLogRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final PaymentRefundRepository refundRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AdapterReportRepository adapterReportRepository;
    private final AdminOverviewService overviewService;
    private final AdminTrendService trendService;
    private final PaymentSummaryService paymentSummaryService;
    private final AnalyticsReportService analyticsReportService;
    private final MemberService memberService;
    private final DeviceService deviceService;
    private final AdminAggregateService adminAggregateService;
    private final AdminUserProfileService adminUserProfileService;
    private final PasswordEncoder passwordEncoder;

    public AdminDataController(UserInfoRepository userInfoRepository,
                               DeviceInfoRepository deviceInfoRepository,
                               DeviceCodeChangeLogRepository deviceCodeChangeLogRepository,
                               MemberInfoRepository memberInfoRepository,
                               PaymentOrderRepository paymentOrderRepository,
                               PaymentCallbackLogRepository callbackLogRepository,
                               PaymentRefundRepository refundRepository,
                               LaunchRecordRepository launchRecordRepository,
                               AdapterReportRepository adapterReportRepository,
                               AdminOverviewService overviewService,
                               AdminTrendService trendService,
                               PaymentSummaryService paymentSummaryService,
                               AnalyticsReportService analyticsReportService,
                               MemberService memberService,
                               DeviceService deviceService,
                               AdminAggregateService adminAggregateService,
                               AdminUserProfileService adminUserProfileService,
                               PasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceCodeChangeLogRepository = deviceCodeChangeLogRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.refundRepository = refundRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.adapterReportRepository = adapterReportRepository;
        this.overviewService = overviewService;
        this.trendService = trendService;
        this.paymentSummaryService = paymentSummaryService;
        this.analyticsReportService = analyticsReportService;
        this.memberService = memberService;
        this.deviceService = deviceService;
        this.adminAggregateService = adminAggregateService;
        this.adminUserProfileService = adminUserProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserInfo>> users(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(required = false) String mobile,
                                             @RequestParam(required = false) String username,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) UserStatus status,
                                             @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<UserInfo> result = userInfoRepository.search(
                trimToNull(keyword),
                trimToNull(mobile),
                trimToNull(username),
                status,
                pageRequest
        );
        return ApiResponse.ok(result);
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<UserInfo> changeUserStatus(@PathVariable Long id,
                                                  @RequestBody ChangeUserStatusRequest request) {
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        UserStatus status = request.status();
        userInfo.changeStatus(status);
        return ApiResponse.ok(userInfoRepository.save(userInfo));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserInfo> userDetail(@PathVariable Long id) {
        return ApiResponse.ok(userInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在")));
    }

    @GetMapping("/users/{id}/profile")
    public ApiResponse<AdminUserProfileResult> userProfile(@PathVariable Long id) {
        return ApiResponse.ok(adminUserProfileService.profile(id));
    }

    @PutMapping("/users/{id}/profile")
    public ApiResponse<UserInfo> updateUserProfile(@PathVariable Long id,
                                                   @javax.validation.Valid @RequestBody UpdateUserProfileAdminRequest request) {
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        String mobile = requireText(request.mobile(), "手机号不能为空");
        String username = normalizeUsername(request.username());
        String nickname = trimToNull(request.nickname());
        userInfoRepository.findByMobile(mobile)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
                });
        if (username != null) {
            userInfoRepository.findByUsername(username)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
                    });
        }
        boolean mobileChanged = !mobile.equals(userInfo.getMobile());
        userInfo.updateProfile(mobile, username, nickname);
        if (mobileChanged) {
            userInfo.bumpTokenVersion();
        }
        return ApiResponse.ok(userInfoRepository.save(userInfo));
    }

    @PostMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetUserPassword(@PathVariable Long id,
                                               @javax.validation.Valid @RequestBody ResetUserPasswordRequest request) {
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        String password = request.password();
        if (password.length() < 8 || password.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度需为 8-64 位");
        }
        userInfo.setPasswordHash(passwordEncoder.encode(password));
        userInfo.requirePasswordReset();
        userInfoRepository.save(userInfo);
        return ApiResponse.ok();
    }

    @GetMapping("/devices")
    public ApiResponse<Page<DeviceInfo>> devices(@RequestParam(required = false) String appId,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String deviceCode,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<DeviceInfo> result;
        if (appId == null || appId.trim().isEmpty()) {
            result = deviceInfoRepository.findAll(pageRequest);
        } else if (deviceCode != null && !deviceCode.trim().isEmpty()) {
            result = deviceInfoRepository.findByAppIdAndDeviceCode(appId, deviceCode, pageRequest);
        } else if (userId != null) {
            result = deviceInfoRepository.findByAppIdAndUserId(appId, userId, pageRequest);
        } else {
            result = deviceInfoRepository.findByAppId(appId, pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/devices/{id}/unbind")
    public ApiResponse<DeviceInfo> unbindDevice(@PathVariable Long id) {
        return ApiResponse.ok(deviceService.unbind(id));
    }

    @PostMapping("/devices/{id}/bind-user")
    public ApiResponse<DeviceInfo> bindDeviceUser(@PathVariable Long id,
                                                  @javax.validation.Valid @RequestBody BindDeviceUserRequest request) {
        return ApiResponse.ok(deviceService.bindUser(id, request.userId()));
    }

    @PatchMapping("/devices/{id}/device-code")
    public ApiResponse<DeviceInfo> changeDeviceCode(@PathVariable Long id,
                                                    @javax.validation.Valid @RequestBody ChangeDeviceCodeRequest request,
                                                    @org.springframework.security.core.annotation.AuthenticationPrincipal com.lianpayhub.security.AdminPrincipal principal) {
        return ApiResponse.ok(deviceService.changeDeviceCode(
                id,
                request.deviceCode(),
                request.reason(),
                principal == null ? null : principal.getAdminId(),
                principal == null ? null : principal.getUsername()
        ));
    }

    @GetMapping("/members")
    public ApiResponse<Page<MemberInfo>> members(@RequestParam(required = false) String appId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<MemberInfo> result = appId == null || appId.trim().isEmpty()
                ? memberInfoRepository.findAll(pageRequest)
                : memberInfoRepository.findByAppId(appId, pageRequest);
        return ApiResponse.ok(result);
    }

    @PostMapping("/members/grant")
    public ApiResponse<MemberInfo> grantMember(@RequestBody GrantMemberRequest request) {
        if (request.subjectType() == MemberSubjectType.USER && request.userId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "赠送账号会员必须提供 userId");
        }
        if (request.subjectType() == MemberSubjectType.DEVICE && request.deviceId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "赠送设备会员必须提供 deviceId");
        }
        return ApiResponse.ok(memberService.grant(new GrantMemberCommand(
                request.appId(),
                request.subjectType(),
                request.userId(),
                request.deviceId(),
                request.packageId(),
                request.durationDays()
        )));
    }

    @PostMapping("/members/{id}/cancel")
    public ApiResponse<MemberInfo> cancelMember(@PathVariable Long id) {
        return ApiResponse.ok(memberService.cancel(id));
    }

    @GetMapping("/orders")
    public ApiResponse<Page<PaymentOrder>> orders(@RequestParam(required = false) String appId,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String orderNo,
                                                  @RequestParam(required = false) String deviceCode,
                                                  @RequestParam(required = false) String tradeNo,
                                                  @RequestParam(required = false) String mobile,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        String searchText = firstText(keyword, orderNo, deviceCode, tradeNo, mobile);
        Page<PaymentOrder> result = searchText == null
                ? (appId == null || appId.trim().isEmpty()
                ? paymentOrderRepository.findAll(pageRequest)
                : paymentOrderRepository.findByAppId(appId, pageRequest))
                : paymentOrderRepository.search(trimToNull(appId), searchText, pageRequest);
        return ApiResponse.ok(result);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<PaymentOrder> orderDetail(@PathVariable Long id) {
        return ApiResponse.ok(paymentOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在")));
    }

    @GetMapping("/payment-callbacks")
    public ApiResponse<Page<PaymentCallbackLog>> paymentCallbacks(@RequestParam(required = false) String appId,
                                                                  @RequestParam(required = false) Long orderId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentCallbackLog> result;
        if (orderId != null) {
            result = callbackLogRepository.findByOrderId(orderId, pageRequest);
        } else if (appId != null && !appId.trim().isEmpty()) {
            result = callbackLogRepository.findByAppId(appId, pageRequest);
        } else {
            result = callbackLogRepository.findAll(pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/payment-refunds")
    public ApiResponse<Page<PaymentRefund>> paymentRefunds(@RequestParam(required = false) String appId,
                                                           @RequestParam(required = false) Long orderId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentRefund> result;
        if (orderId != null) {
            result = refundRepository.findByOrderId(orderId, pageRequest);
        } else if (appId != null && !appId.trim().isEmpty()) {
            result = refundRepository.findByAppId(appId, pageRequest);
        } else {
            result = refundRepository.findAll(pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/launch-records")
    public ApiResponse<Page<LaunchRecord>> launchRecords(@RequestParam(required = false) String appId,
                                                         @RequestParam(required = false) Long deviceId,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<LaunchRecord> result;
        if (appId == null || appId.trim().isEmpty()) {
            result = launchRecordRepository.findAll(pageRequest);
        } else if (deviceId != null) {
            result = launchRecordRepository.findByAppIdAndDeviceId(appId, deviceId, pageRequest);
        } else if (userId != null) {
            result = launchRecordRepository.findByAppIdAndUserId(appId, userId, pageRequest);
        } else {
            result = launchRecordRepository.findByAppId(appId, pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/adapter-reports")
    public ApiResponse<Page<AdapterReport>> adapterReports(@RequestParam(required = false) String appId,
                                                           @RequestParam(required = false) String sourceId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<AdapterReport> result;
        if (appId == null || appId.trim().isEmpty()) {
            result = adapterReportRepository.findAll(pageRequest);
        } else if (sourceId != null && !sourceId.trim().isEmpty()) {
            result = adapterReportRepository.findByAppIdAndSourceId(appId, sourceId, pageRequest);
        } else {
            result = adapterReportRepository.findByAppId(appId, pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/adapter-reports/{id}")
    public ApiResponse<AdapterReport> adapterReportDetail(@PathVariable Long id) {
        return ApiResponse.ok(adapterReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "适配上报不存在")));
    }

    @PostMapping("/adapter-reports/{id}/mark-processed")
    public ApiResponse<AdapterReport> markAdapterProcessed(@PathVariable Long id) {
        AdapterReport report = adapterReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "适配上报不存在"));
        report.markProcessed();
        return ApiResponse.ok(adapterReportRepository.save(report));
    }

    @PostMapping("/adapter-reports/{id}/mark-failed")
    public ApiResponse<AdapterReport> markAdapterFailed(@PathVariable Long id) {
        AdapterReport report = adapterReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "适配上报不存在"));
        report.markFailed();
        return ApiResponse.ok(adapterReportRepository.save(report));
    }

    @GetMapping("/devices/{id}")
    public ApiResponse<DeviceInfo> deviceDetail(@PathVariable Long id) {
        return ApiResponse.ok(deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在")));
    }

    @GetMapping("/devices/{id}/aggregate")
    public ApiResponse<DeviceAggregateResult> deviceAggregate(@PathVariable Long id) {
        return ApiResponse.ok(adminAggregateService.deviceAggregate(id));
    }

    @GetMapping("/devices/{id}/device-code-logs")
    public ApiResponse<Page<com.lianpayhub.domain.device.DeviceCodeChangeLog>> deviceCodeLogs(@PathVariable Long id,
                                                                                              @RequestParam(defaultValue = "0") int page,
                                                                                              @RequestParam(defaultValue = "20") int size) {
        deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "设备不存在"));
        return ApiResponse.ok(deviceCodeChangeLogRepository.findByDeviceId(id, pageRequest(page, size)));
    }

    @GetMapping("/launch-records/{id}")
    public ApiResponse<LaunchRecord> launchRecordDetail(@PathVariable Long id) {
        return ApiResponse.ok(launchRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "启动记录不存在")));
    }

    @GetMapping("/reports/overview")
    public ApiResponse<AdminOverviewResult> overview() {
        return ApiResponse.ok(overviewService.overview());
    }

    @GetMapping("/reports/trend")
    public ApiResponse<java.util.List<DailyTrendItem>> trend(@RequestParam(defaultValue = "14") int days) {
        return ApiResponse.ok(trendService.dailyTrend(days));
    }

    @GetMapping("/reports/payment-summary")
    public ApiResponse<PaymentSummaryResult> paymentSummary() {
        return ApiResponse.ok(paymentSummaryService.summary());
    }

    @GetMapping("/reports/analytics")
    public ApiResponse<AnalyticsResult> analytics(@RequestParam(defaultValue = "DAY") AnalyticsGranularity granularity,
                                                  @RequestParam(defaultValue = "ORDER_COUNT") AnalyticsMetric metric,
                                                  @RequestParam(required = false) String appId,
                                                  @RequestParam(defaultValue = "30") int periods) {
        return ApiResponse.ok(analyticsReportService.analytics(granularity, metric, appId, periods));
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return trimmed;
    }

    private String normalizeUsername(String username) {
        String value = trimToNull(username);
        if (value == null) {
            return null;
        }
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        if (!normalized.matches("^[a-z0-9_][a-z0-9_.-]{2,31}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名需为 3-32 位字母、数字、点、横线或下划线");
        }
        return normalized;
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
