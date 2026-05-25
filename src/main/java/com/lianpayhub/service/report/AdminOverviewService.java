package com.lianpayhub.service.report;

import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOverviewService {

    private final AppInfoRepository appInfoRepository;
    private final UserInfoRepository userInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AdapterReportRepository adapterReportRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final PaymentEventLogRepository paymentEventLogRepository;

    public AdminOverviewService(AppInfoRepository appInfoRepository,
                                UserInfoRepository userInfoRepository,
                                DeviceInfoRepository deviceInfoRepository,
                                MemberInfoRepository memberInfoRepository,
                                PaymentOrderRepository paymentOrderRepository,
                                LaunchRecordRepository launchRecordRepository,
                                AdapterReportRepository adapterReportRepository,
                                AppLoginLogRepository appLoginLogRepository,
                                PaymentEventLogRepository paymentEventLogRepository) {
        this.appInfoRepository = appInfoRepository;
        this.userInfoRepository = userInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.adapterReportRepository = adapterReportRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.paymentEventLogRepository = paymentEventLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResult overview() {
        Long paidAmount = paymentOrderRepository.sumAmountCentsByPayStatus(PayStatus.PAID);
        return new AdminOverviewResult(
                appInfoRepository.count(),
                userInfoRepository.count(),
                deviceInfoRepository.count(),
                memberInfoRepository.count(),
                paymentOrderRepository.count(),
                paymentOrderRepository.countByPayStatus(PayStatus.PAID),
                paidAmount == null ? 0L : paidAmount,
                launchRecordRepository.count(),
                adapterReportRepository.count(),
                appLoginLogRepository.count(),
                paymentEventLogRepository.count()
        );
    }
}
