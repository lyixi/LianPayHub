package com.lianpayhub.web.admin;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdatePackageRequest {
    @NotBlank
    private String packageName;
    @NotNull
    @Min(0)
    private Integer priceCents;
    @NotNull
    @Min(1)
    private Integer durationDays;
    private String benefitsText;

    public String packageName() { return packageName; }
    public Integer priceCents() { return priceCents; }
    public Integer durationDays() { return durationDays; }
    public String benefitsText() { return benefitsText; }
}
