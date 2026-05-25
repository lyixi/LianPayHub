package com.lianpayhub.domain.packageinfo;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "package_info", indexes = {
        @Index(name = "idx_package_info_app", columnList = "app_id")
})
public class PackageInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "package_name", nullable = false, length = 128)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 32)
    private PackageType packageType;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "benefits_text", length = 1024)
    private String benefitsText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PackageStatus status = PackageStatus.ENABLED;

    protected PackageInfo() {
    }

    public PackageInfo(String appId, String packageName, PackageType packageType, Integer priceCents,
                       Integer durationDays, String benefitsText) {
        this.appId = appId;
        this.packageName = packageName;
        this.packageType = packageType;
        this.priceCents = priceCents;
        this.durationDays = durationDays;
        this.benefitsText = benefitsText;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getPackageName() {
        return packageName;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public Integer getPriceCents() {
        return priceCents;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public String getBenefitsText() {
        return benefitsText;
    }

    public void update(String packageName, Integer priceCents, Integer durationDays, String benefitsText) {
        this.packageName = packageName;
        this.priceCents = priceCents;
        this.durationDays = durationDays;
        this.benefitsText = benefitsText;
    }

    public void changeStatus(PackageStatus status) {
        this.status = status;
    }
}
