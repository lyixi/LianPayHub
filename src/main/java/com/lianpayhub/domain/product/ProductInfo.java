package com.lianpayhub.domain.product;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "product_info", indexes = {
        @Index(name = "uk_product_info_app_code", columnList = "app_id,product_code", unique = true),
        @Index(name = "idx_product_info_app", columnList = "app_id")
})
public class ProductInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 32)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 32)
    private FulfillmentType fulfillmentType;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "benefits_text", length = 2000)
    private String benefitsText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductStatus status = ProductStatus.ENABLED;

    protected ProductInfo() {
    }

    public ProductInfo(String appId, String productCode, String productName, ProductType productType,
                       FulfillmentType fulfillmentType, String description, String benefitsText, Integer sortOrder) {
        this.appId = appId;
        this.productCode = productCode;
        this.productName = productName;
        this.productType = productType;
        this.fulfillmentType = fulfillmentType;
        this.description = description;
        this.benefitsText = benefitsText;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public ProductType getProductType() { return productType; }
    public FulfillmentType getFulfillmentType() { return fulfillmentType; }
    public String getDescription() { return description; }
    public String getBenefitsText() { return benefitsText; }
    public Integer getSortOrder() { return sortOrder; }
    public ProductStatus getStatus() { return status; }

    public void update(String productName, String description, String benefitsText, Integer sortOrder) {
        this.productName = productName;
        this.description = description;
        this.benefitsText = benefitsText;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
}
