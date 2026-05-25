package com.lianpayhub.service.report;

import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.repository.AppLoginLogRepository;
import com.lianpayhub.repository.LaunchRecordRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminTrendService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final LaunchRecordRepository launchRecordRepository;

    public AdminTrendService(PaymentOrderRepository paymentOrderRepository,
                             AppLoginLogRepository appLoginLogRepository,
                             LaunchRecordRepository launchRecordRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.launchRecordRepository = launchRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<DailyTrendItem> dailyTrend(int days) {
        int safeDays = Math.min(Math.max(days, 1), 90);
        LocalDate startDate = LocalDate.now().minusDays(safeDays - 1L);
        List<DailyTrendItem> result = new ArrayList<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            Long amount = paymentOrderRepository.sumAmountCentsByPayStatusAndPaidAtBetween(PayStatus.PAID, start, end);
            result.add(new DailyTrendItem(
                    date.toString(),
                    paymentOrderRepository.countByCreatedAtBetween(start, end),
                    paymentOrderRepository.countByPayStatusAndPaidAtBetween(PayStatus.PAID, start, end),
                    amount == null ? 0L : amount,
                    appLoginLogRepository.countByCreatedAtBetween(start, end),
                    launchRecordRepository.countByCreatedAtBetween(start, end)
            ));
        }
        return result;
    }
}
