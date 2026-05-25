package com.lianpayhub.service.demo;

public class DemoDataResult {
    private final String appId;
    private final Long packageId;
    private final Long deviceId;
    private final String orderNo;
    private final String message;

    public DemoDataResult(String appId, Long packageId, Long deviceId, String orderNo, String message) {
        this.appId = appId;
        this.packageId = packageId;
        this.deviceId = deviceId;
        this.orderNo = orderNo;
        this.message = message;
    }

    public String getAppId() { return appId; }
    public Long getPackageId() { return packageId; }
    public Long getDeviceId() { return deviceId; }
    public String getOrderNo() { return orderNo; }
    public String getMessage() { return message; }
}
