package com.lianpayhub.service.payment;

import com.lianpayhub.config.PaymentProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderCloseScheduler {

    private final PaymentProperties paymentProperties;
    private final PaymentService paymentService;

    public PaymentOrderCloseScheduler(PaymentProperties paymentProperties, PaymentService paymentService) {
        this.paymentProperties = paymentProperties;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${lianpayhub.payment.order-close-scan-fixed-delay-ms:60000}",
            initialDelayString = "${lianpayhub.payment.order-close-scan-fixed-delay-ms:60000}")
    public void closeExpiredOrders() {
        if (!Boolean.TRUE.equals(paymentProperties.getOrderAutoCloseEnabled())) {
            return;
        }
        paymentService.closeExpiredOrders();
    }
}
