package com.lianpayhub.service.report;

import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.domain.payment.RefundStatus;
import com.lianpayhub.repository.AdapterReportRepository;
import com.lianpayhub.repository.AppLoginLogRepository;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.LaunchRecordRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.repository.PaymentRefundRepository;
import com.lianpayhub.repository.UserInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsReportService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PaymentOrderRepository paymentOrderRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final AdapterReportRepository adapterReportRepository;
    private final UserInfoRepository userInfoRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    public AnalyticsReportService(PaymentOrderRepository paymentOrderRepository,
                                  LaunchRecordRepository launchRecordRepository,
                                  AppLoginLogRepository appLoginLogRepository,
                                  PaymentRefundRepository paymentRefundRepository,
                                  AdapterReportRepository adapterReportRepository,
                                  UserInfoRepository userInfoRepository,
                                  DeviceInfoRepository deviceInfoRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.adapterReportRepository = adapterReportRepository;
        this.userInfoRepository = userInfoRepository;
        this.deviceInfoRepository = deviceInfoRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResult analytics(AnalyticsGranularity granularity, AnalyticsMetric metric,
                                     String appId, int periods) {
        AnalyticsGranularity safeGranularity = granularity == null ? AnalyticsGranularity.DAY : granularity;
        AnalyticsMetric safeMetric = metric == null ? AnalyticsMetric.ORDER_COUNT : metric;
        int safePeriods = safePeriods(safeGranularity, periods);
        String safeAppId = normalizeAppId(appId);
        LocalDate cursor = firstPeriodStart(safeGranularity, safePeriods);
        List<AnalyticsPoint> points = new ArrayList<>();
        long totalValue = 0L;
        long totalAmountCents = 0L;

        for (int i = 0; i < safePeriods; i++) {
            LocalDate startDate = cursor;
            LocalDate endDate = nextPeriod(safeGranularity, startDate);
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atStartOfDay();
            long amountCents = amountFor(safeMetric, safeAppId, start, end);
            long value = valueFor(safeMetric, safeAppId, start, end, amountCents);
            points.add(new AnalyticsPoint(formatPeriod(safeGranularity, startDate), value, amountCents));
            totalValue += value;
            totalAmountCents += amountCents;
            cursor = endDate;
        }

        return new AnalyticsResult(safeGranularity, safeMetric, safeAppId, points, totalValue, totalAmountCents);
    }

    private int safePeriods(AnalyticsGranularity granularity, int periods) {
        int max = granularity == AnalyticsGranularity.DAY ? 90 : 36;
        if (granularity == AnalyticsGranularity.YEAR) {
            max = 10;
        }
        return Math.min(Math.max(periods, 1), max);
    }

    private LocalDate firstPeriodStart(AnalyticsGranularity granularity, int periods) {
        LocalDate today = LocalDate.now();
        if (granularity == AnalyticsGranularity.MONTH) {
            return YearMonth.from(today).minusMonths(periods - 1L).atDay(1);
        }
        if (granularity == AnalyticsGranularity.YEAR) {
            return LocalDate.of(today.getYear() - periods + 1, 1, 1);
        }
        return today.minusDays(periods - 1L);
    }

    private LocalDate nextPeriod(AnalyticsGranularity granularity, LocalDate start) {
        if (granularity == AnalyticsGranularity.MONTH) {
            return start.plusMonths(1);
        }
        if (granularity == AnalyticsGranularity.YEAR) {
            return start.plusYears(1);
        }
        return start.plusDays(1);
    }

    private String formatPeriod(AnalyticsGranularity granularity, LocalDate start) {
        if (granularity == AnalyticsGranularity.MONTH) {
            return MONTH_FORMATTER.format(start);
        }
        if (granularity == AnalyticsGranularity.YEAR) {
            return String.valueOf(start.getYear());
        }
        return start.toString();
    }

    private long valueFor(AnalyticsMetric metric, String appId, LocalDateTime start, LocalDateTime end,
                          long amountCents) {
        if (metric == AnalyticsMetric.PAID_AMOUNT || metric == AnalyticsMetric.REFUND_AMOUNT) {
            return amountCents;
        }
        if (metric == AnalyticsMetric.ORDER_COUNT) {
            return countOrders(appId, start, end);
        }
        if (metric == AnalyticsMetric.PAID_ORDER_COUNT) {
            return countPaidOrders(appId, start, end);
        }
        if (metric == AnalyticsMetric.LAUNCH_COUNT) {
            return countLaunches(appId, start, end);
        }
        if (metric == AnalyticsMetric.LOGIN_COUNT) {
            return countLogins(appId, start, end);
        }
        if (metric == AnalyticsMetric.REFUND_COUNT) {
            return countRefunds(appId, start, end);
        }
        if (metric == AnalyticsMetric.ADAPTER_REPORT_COUNT) {
            return countAdapterReports(appId, start, end);
        }
        if (metric == AnalyticsMetric.NEW_USER_COUNT) {
            return countNewUsers(start, end);
        }
        if (metric == AnalyticsMetric.NEW_DEVICE_COUNT) {
            return countNewDevices(appId, start, end);
        }
        return 0L;
    }

    private long amountFor(AnalyticsMetric metric, String appId, LocalDateTime start, LocalDateTime end) {
        if (metric == AnalyticsMetric.PAID_AMOUNT) {
            Long amount = appId == null
                    ? paymentOrderRepository.sumAmountCentsByPayStatusAndPaidAtBetween(PayStatus.PAID, start, end)
                    : paymentOrderRepository.sumAmountCentsByAppIdAndPayStatusAndPaidAtBetween(appId, PayStatus.PAID, start, end);
            return amount == null ? 0L : amount;
        }
        if (metric == AnalyticsMetric.REFUND_AMOUNT) {
            Long amount = appId == null
                    ? paymentRefundRepository.sumAmountCentsByStatusAndProcessedAtBetween(RefundStatus.SUCCESS, start, end)
                    : paymentRefundRepository.sumAmountCentsByAppIdAndStatusAndProcessedAtBetween(appId, RefundStatus.SUCCESS, start, end);
            return amount == null ? 0L : amount;
        }
        return 0L;
    }

    private long countOrders(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? paymentOrderRepository.countByCreatedAtBetween(start, end)
                : paymentOrderRepository.countByAppIdAndCreatedAtBetween(appId, start, end);
    }

    private long countPaidOrders(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? paymentOrderRepository.countByPayStatusAndPaidAtBetween(PayStatus.PAID, start, end)
                : paymentOrderRepository.countByAppIdAndPayStatusAndPaidAtBetween(appId, PayStatus.PAID, start, end);
    }

    private long countLaunches(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? launchRecordRepository.countByCreatedAtBetween(start, end)
                : launchRecordRepository.countByAppIdAndCreatedAtBetween(appId, start, end);
    }

    private long countLogins(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? appLoginLogRepository.countByCreatedAtBetween(start, end)
                : appLoginLogRepository.countByAppIdAndCreatedAtBetween(appId, start, end);
    }

    private long countRefunds(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? paymentRefundRepository.countByStatusAndProcessedAtBetween(RefundStatus.SUCCESS, start, end)
                : paymentRefundRepository.countByAppIdAndStatusAndProcessedAtBetween(appId, RefundStatus.SUCCESS, start, end);
    }

    private long countAdapterReports(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? adapterReportRepository.countByCreatedAtBetween(start, end)
                : adapterReportRepository.countByAppIdAndCreatedAtBetween(appId, start, end);
    }

    private long countNewUsers(LocalDateTime start, LocalDateTime end) {
        return userInfoRepository.countByCreatedAtBetween(start, end);
    }

    private long countNewDevices(String appId, LocalDateTime start, LocalDateTime end) {
        return appId == null
                ? deviceInfoRepository.countByCreatedAtBetween(start, end)
                : deviceInfoRepository.countByAppIdAndCreatedAtBetween(appId, start, end);
    }

    private String normalizeAppId(String appId) {
        if (appId == null || appId.trim().isEmpty()) {
            return null;
        }
        return appId.trim();
    }
}
