package com.lianpayhub.service.report;

import java.util.List;

public class PaymentSummaryResult {

    private final List<PaymentSummaryItem> byApp;
    private final List<PaymentSummaryItem> byPayChannel;

    public PaymentSummaryResult(List<PaymentSummaryItem> byApp, List<PaymentSummaryItem> byPayChannel) {
        this.byApp = byApp;
        this.byPayChannel = byPayChannel;
    }

    public List<PaymentSummaryItem> getByApp() { return byApp; }
    public List<PaymentSummaryItem> getByPayChannel() { return byPayChannel; }
}
