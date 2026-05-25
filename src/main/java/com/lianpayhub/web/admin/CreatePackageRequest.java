package com.lianpayhub.web.admin;

import com.lianpayhub.domain.packageinfo.PackageType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreatePackageRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String packageName;
    @NotNull
    private PackageType packageType;
    @NotNull
    @Min(0)
    private Integer priceCents;
    @NotNull
    @Min(1)
    private Integer durationDays;
    private String benefitsText;

    public String appId() { return appId; }
    public String packageName() { return packageName; }
    public PackageType packageType() { return packageType; }
    public Integer priceCents() { return priceCents; }
    public Integer durationDays() { return durationDays; }
    public String benefitsText() { return benefitsText; }
}
