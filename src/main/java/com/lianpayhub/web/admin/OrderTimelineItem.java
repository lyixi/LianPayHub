package com.lianpayhub.web.admin;

import java.time.LocalDateTime;

public class OrderTimelineItem {
    private final String sourceType;
    private final Long sourceId;
    private final String title;
    private final String description;
    private final String status;
    private final LocalDateTime happenedAt;
    private final Integer amountCents;

    public OrderTimelineItem(String sourceType, Long sourceId, String title, String description,
                             String status, LocalDateTime happenedAt, Integer amountCents) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.happenedAt = happenedAt;
        this.amountCents = amountCents;
    }

    public String getSourceType() { return sourceType; }
    public Long getSourceId() { return sourceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getHappenedAt() { return happenedAt; }
    public Integer getAmountCents() { return amountCents; }
}
