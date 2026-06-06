package com.lianpayhub.service.report;

import java.util.List;

public class AnalyticsResult {
    private final AnalyticsGranularity granularity;
    private final AnalyticsMetric metric;
    private final String appId;
    private final List<AnalyticsPoint> points;
    private final long totalValue;
    private final long totalAmountCents;

    public AnalyticsResult(AnalyticsGranularity granularity, AnalyticsMetric metric, String appId,
                           List<AnalyticsPoint> points, long totalValue, long totalAmountCents) {
        this.granularity = granularity;
        this.metric = metric;
        this.appId = appId;
        this.points = points;
        this.totalValue = totalValue;
        this.totalAmountCents = totalAmountCents;
    }

    public AnalyticsGranularity getGranularity() { return granularity; }
    public AnalyticsMetric getMetric() { return metric; }
    public String getAppId() { return appId; }
    public List<AnalyticsPoint> getPoints() { return points; }
    public long getTotalValue() { return totalValue; }
    public long getTotalAmountCents() { return totalAmountCents; }
}
