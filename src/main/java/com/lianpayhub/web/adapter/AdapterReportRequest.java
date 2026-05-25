package com.lianpayhub.web.adapter;

import javax.validation.constraints.NotBlank;

public class AdapterReportRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String sourceId;
    @NotBlank
    private String reportType;
    @NotBlank
    private String payload;

    public String appId() { return appId; }
    public String sourceId() { return sourceId; }
    public String reportType() { return reportType; }
    public String payload() { return payload; }
}
