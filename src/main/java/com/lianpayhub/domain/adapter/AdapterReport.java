package com.lianpayhub.domain.adapter;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "adapter_report", indexes = {
        @Index(name = "idx_adapter_report_app_source", columnList = "app_id,source_id")
})
public class AdapterReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "source_id", nullable = false, length = 128)
    private String sourceId;

    @Column(name = "report_type", nullable = false, length = 64)
    private String reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdapterReportStatus status = AdapterReportStatus.RECEIVED;

    @Lob
    @Column(nullable = false)
    private String payload;

    protected AdapterReport() {
    }

    public AdapterReport(String appId, String sourceId, String reportType, String payload) {
        this.appId = appId;
        this.sourceId = sourceId;
        this.reportType = reportType;
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getReportType() {
        return reportType;
    }

    public AdapterReportStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public void markProcessed() {
        this.status = AdapterReportStatus.PROCESSED;
    }

    public void markFailed() {
        this.status = AdapterReportStatus.FAILED;
    }
}
