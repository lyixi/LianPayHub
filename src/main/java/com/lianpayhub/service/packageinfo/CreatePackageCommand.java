package com.lianpayhub.service.packageinfo;

import com.lianpayhub.domain.packageinfo.PackageType;

public class CreatePackageCommand {
    private final String appId;
    private final String packageName;
    private final PackageType packageType;
    private final Integer priceCents;
    private final Integer durationDays;
    private final String benefitsText;

    public CreatePackageCommand(String appId, String packageName, PackageType packageType,
                                Integer priceCents, Integer durationDays, String benefitsText) {
        this.appId = appId;
        this.packageName = packageName;
        this.packageType = packageType;
        this.priceCents = priceCents;
        this.durationDays = durationDays;
        this.benefitsText = benefitsText;
    }

    public String appId() { return appId; }
    public String packageName() { return packageName; }
    public PackageType packageType() { return packageType; }
    public Integer priceCents() { return priceCents; }
    public Integer durationDays() { return durationDays; }
    public String benefitsText() { return benefitsText; }
}
