package com.lianpayhub.service.report;

import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.repository.PaymentOrderRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSummaryService {

    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentSummaryService(PaymentOrderRepository paymentOrderRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResult summary() {
        List<PaymentSummaryItem> byApp = toItems(paymentOrderRepository.summarizeByApp(PayStatus.PAID));
        List<PaymentSummaryItem> byPayChannel = toItems(paymentOrderRepository.summarizeByPayChannel(PayStatus.PAID));
        sortByAmountDesc(byApp);
        sortByAmountDesc(byPayChannel);
        return new PaymentSummaryResult(byApp, byPayChannel);
    }

    private List<PaymentSummaryItem> toItems(List<Object[]> rows) {
        List<PaymentSummaryItem> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new PaymentSummaryItem(
                    String.valueOf(row[0]),
                    longValue(row[1]),
                    longValue(row[2]),
                    longValue(row[3])
            ));
        }
        return result;
    }

    private void sortByAmountDesc(List<PaymentSummaryItem> items) {
        Collections.sort(items, new Comparator<PaymentSummaryItem>() {
            @Override
            public int compare(PaymentSummaryItem left, PaymentSummaryItem right) {
                int amountCompare = Long.compare(right.getPaidAmountCents(), left.getPaidAmountCents());
                return amountCompare != 0 ? amountCompare : Long.compare(right.getOrderCount(), left.getOrderCount());
            }
        });
    }

    private long longValue(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
