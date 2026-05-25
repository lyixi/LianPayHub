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
import com.lianpayhub.service.report.AdminOverviewResult;
import com.lianpayhub.service.report.AdminOverviewService;
import com.lianpayhub.service.report.AdminTrendService;
import com.lianpayhub.service.report.DailyTrendItem;
import com.lianpayhub.service.report.PaymentSummaryResult;
import com.lianpayhub.service.report.PaymentSummaryService;
import com.lianpayhub.service.member.GrantMemberCommand;
import com.lianpayhub.service.member.MemberService;
import com.lianpayhub.service.device.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminDataController {

    private final UserInfoRepository userInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final PaymentRefundRepository refundRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AdapterReportRepository adapterReportRepository;
    private final AdminOverviewService overviewService;
    private final AdminTrendService trendService;
    private final PaymentSummaryService paymentSummaryService;
    private final MemberService memberService;
    private final DeviceService deviceService;

    public AdminDataController(UserInfoRepository userInfoRepository,
                               DeviceInfoRepository deviceInfoRepository,
                               MemberInfoRepository memberInfoRepository,
                               PaymentOrderRepository paymentOrderRepository,
                               PaymentCallbackLogRepository callbackLogRepository,
                               PaymentRefundRepository refundRepository,
                               LaunchRecordRepository launchRecordRepository,
                               AdapterReportRepository adapterReportRepository,
                               AdminOverviewService overviewService,
                               AdminTrendService trendService,
                               PaymentSummaryService paymentSummaryService,
                               MemberService memberService,
                               DeviceService deviceService) {
        this.userInfoRepository = userInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.callbackLogRepository = callbackLogRepository;
        this.refundRepository = refundRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.adapterReportRepository = adapterReportRepository;
        this.overviewService = overviewService;
        this.trendService = trendService;
        this.paymentSummaryService = paymentSummaryService;
        this.memberService = memberService;
        this.deviceService = deviceService;
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserInfo>> users(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(required = false) String mobile,
                                             @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<UserInfo> result = mobile == null || mobile.trim().isEmpty()
                ? userInfoRepository.findAll(pageRequest)
                : userInfoRepository.findByMobile(mobile, pageRequest);
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
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentOrder> result = appId == null || appId.trim().isEmpty()
                ? paymentOrderRepository.findAll(pageRequest)
                : paymentOrderRepository.findByAppId(appId, pageRequest);
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

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
