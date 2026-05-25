package com.lianpayhub.domain.app;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "app_info", indexes = {
        @Index(name = "idx_app_info_app_id", columnList = "app_id", unique = true)
})
public class AppInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64, unique = true)
    private String appId;

    @Column(name = "app_name", nullable = false, length = 128)
    private String appName;

    @Column(name = "app_secret_hash", nullable = false, length = 128)
    private String appSecretHash;

    @Column(name = "app_secret_version", nullable = false)
    private Integer appSecretVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_type", nullable = false, length = 32)
    private AppType appType;

    @Column(name = "need_mobile_login", nullable = false)
    private boolean needMobileLogin;

    @Column(name = "need_device_vip", nullable = false)
    private boolean needDeviceVip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppStatus status = AppStatus.ENABLED;

    protected AppInfo() {
    }

    public AppInfo(String appId, String appName, String appSecretHash, AppType appType,
                   boolean needMobileLogin, boolean needDeviceVip) {
        this.appId = appId;
        this.appName = appName;
        this.appSecretHash = appSecretHash;
        this.appType = appType;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public AppType getAppType() {
        return appType;
    }

    public boolean isNeedMobileLogin() {
        return needMobileLogin;
    }

    public boolean isNeedDeviceVip() {
        return needDeviceVip;
    }

    public AppStatus getStatus() {
        return status;
    }

    public Integer getAppSecretVersion() {
        return appSecretVersion;
    }

    public String getAppSecretHash() {
        return appSecretHash;
    }

    public void rename(String appName) {
        this.appName = appName;
    }

    public void update(String appName, boolean needMobileLogin, boolean needDeviceVip) {
        this.appName = appName;
        this.needMobileLogin = needMobileLogin;
        this.needDeviceVip = needDeviceVip;
    }

    public void changeStatus(AppStatus status) {
        this.status = status;
    }

    public void resetSecret(String appSecretHash) {
        this.appSecretHash = appSecretHash;
        this.appSecretVersion = this.appSecretVersion + 1;
    }
}
