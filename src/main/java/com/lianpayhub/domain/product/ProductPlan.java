package com.lianpayhub.domain.product;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "product_plan", indexes = {
        @Index(name = "uk_product_plan_product_code", columnList = "product_id,plan_code", unique = true),
        @Index(name = "idx_product_plan_app", columnList = "app_id"),
        @Index(name = "idx_product_plan_product", columnList = "product_id")
})
public class ProductPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "plan_code", nullable = false, length = 64)
    private String planCode;

    @Column(name = "plan_name", nullable = false, length = 128)
    private String planName;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(name = "original_price_cents")
    private Integer originalPriceCents;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "credit_amount")
    private Integer creditAmount;

    @Column(name = "badge_text", length = 64)
    private String badgeText;

    @Column(name = "benefits_text", length = 2000)
    private String benefitsText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "legacy_package_id")
    private Long legacyPackageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductPlanStatus status = ProductPlanStatus.ENABLED;

    protected ProductPlan() {
    }

    public ProductPlan(Long productId, String appId, String planCode, String planName, Integer priceCents,
                       Integer originalPriceCents, Integer durationDays, Integer creditAmount,
                       String badgeText, String benefitsText, Integer sortOrder, Long legacyPackageId) {
        this.productId = productId;
        this.appId = appId;
        this.planCode = planCode;
        this.planName = planName;
        this.priceCents = priceCents;
        this.originalPriceCents = originalPriceCents;
        this.durationDays = durationDays;
        this.creditAmount = creditAmount;
        this.badgeText = badgeText;
        this.benefitsText = benefitsText;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.legacyPackageId = legacyPackageId;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getAppId() { return appId; }
    public String getPlanCode() { return planCode; }
    public String getPlanName() { return planName; }
    public Integer getPriceCents() { return priceCents; }
    public Integer getOriginalPriceCents() { return originalPriceCents; }
    public Integer getDurationDays() { return durationDays; }
    public Integer getCreditAmount() { return creditAmount; }
    public String getBadgeText() { return badgeText; }
    public String getBenefitsText() { return benefitsText; }
    public Integer getSortOrder() { return sortOrder; }
    public Long getLegacyPackageId() { return legacyPackageId; }
    public ProductPlanStatus getStatus() { return status; }

    public void update(String planName, Integer priceCents, Integer originalPriceCents, Integer durationDays,
                       Integer creditAmount, String badgeText, String benefitsText, Integer sortOrder) {
        this.planName = planName;
        this.priceCents = priceCents;
        this.originalPriceCents = originalPriceCents;
        this.durationDays = durationDays;
        this.creditAmount = creditAmount;
        this.badgeText = badgeText;
        this.benefitsText = benefitsText;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public void changeStatus(ProductPlanStatus status) {
        this.status = status;
    }
}
