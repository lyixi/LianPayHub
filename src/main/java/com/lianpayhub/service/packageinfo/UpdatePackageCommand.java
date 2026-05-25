package com.lianpayhub.service.packageinfo;

public class UpdatePackageCommand {
    private final String packageName;
    private final Integer priceCents;
    private final Integer durationDays;
    private final String benefitsText;

    public UpdatePackageCommand(String packageName, Integer priceCents, Integer durationDays, String benefitsText) {
        this.packageName = packageName;
        this.priceCents = priceCents;
        this.durationDays = durationDays;
        this.benefitsText = benefitsText;
    }

    public String packageName() { return packageName; }
    public Integer priceCents() { return priceCents; }
    public Integer durationDays() { return durationDays; }
    public String benefitsText() { return benefitsText; }
}
