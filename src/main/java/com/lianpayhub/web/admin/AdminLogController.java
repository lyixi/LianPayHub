package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.launch.LaunchRecord;
import com.lianpayhub.domain.log.AdminOperationLog;
import com.lianpayhub.domain.log.AppLoginLog;
import com.lianpayhub.domain.log.PaymentEventLog;
import com.lianpayhub.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminOperationLogRepository adminOperationLogRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final PaymentEventLogRepository paymentEventLogRepository;

    public AdminLogController(AdminOperationLogRepository adminOperationLogRepository,
                              AppLoginLogRepository appLoginLogRepository,
                              LaunchRecordRepository launchRecordRepository,
                              PaymentEventLogRepository paymentEventLogRepository) {
        this.adminOperationLogRepository = adminOperationLogRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.paymentEventLogRepository = paymentEventLogRepository;
    }

    @GetMapping("/admin-operations")
    public ApiResponse<Page<AdminOperationLog>> adminOperations(@RequestParam(required = false) Long adminId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<AdminOperationLog> result = adminId == null
                ? adminOperationLogRepository.findAll(pageRequest)
                : adminOperationLogRepository.findByAdminId(adminId, pageRequest);
        return ApiResponse.ok(result);
    }

    @GetMapping("/app-logins")
    public ApiResponse<Page<AppLoginLog>> appLogins(@RequestParam(required = false) String appId,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String mobile,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<AppLoginLog> result;
        if (userId != null) {
            result = appLoginLogRepository.findByUserId(userId, pageRequest);
        } else if (mobile != null && !mobile.trim().isEmpty() && appId != null && !appId.trim().isEmpty()) {
            result = appLoginLogRepository.findByAppIdAndMobile(appId, mobile, pageRequest);
        } else if (mobile != null && !mobile.trim().isEmpty()) {
            result = appLoginLogRepository.findByMobile(mobile, pageRequest);
        } else if (appId != null && !appId.trim().isEmpty()) {
            result = appLoginLogRepository.findByAppId(appId, pageRequest);
        } else {
            result = appLoginLogRepository.findAll(pageRequest);
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/launches")
    public ApiResponse<Page<LaunchRecord>> launches(@RequestParam(required = false) String appId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<LaunchRecord> result = appId == null || appId.trim().isEmpty()
                ? launchRecordRepository.findAll(pageRequest)
                : launchRecordRepository.findByAppId(appId, pageRequest);
        return ApiResponse.ok(result);
    }

    @GetMapping("/payment-events")
    public ApiResponse<Page<PaymentEventLog>> paymentEvents(@RequestParam(required = false) String appId,
                                                            @RequestParam(required = false) Long orderId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentEventLog> result;
        if (orderId != null) {
            result = paymentEventLogRepository.findByOrderId(orderId, pageRequest);
        } else if (appId != null && !appId.trim().isEmpty()) {
            result = paymentEventLogRepository.findByAppId(appId, pageRequest);
        } else {
            result = paymentEventLogRepository.findAll(pageRequest);
        }
        return ApiResponse.ok(result);
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
