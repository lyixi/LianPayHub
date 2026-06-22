package com.lianpayhub.domain.purchase;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "purchase_page_config", indexes = {
        @Index(name = "uk_purchase_page_slug", columnList = "page_slug", unique = true),
        @Index(name = "idx_purchase_page_app", columnList = "app_id")
})
public class PurchasePageConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "page_slug", nullable = false, length = 128, unique = true)
    private String pageSlug;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "subtitle", length = 512)
    private String subtitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false, length = 32)
    private PurchaseLayoutType layoutType = PurchaseLayoutType.CARD_GRID;

    @Lob
    @Column(name = "theme_json")
    private String themeJson;

    @Lob
    @Column(name = "content_json")
    private String contentJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PurchasePageStatus status = PurchasePageStatus.DRAFT;

    protected PurchasePageConfig() {
    }

    public PurchasePageConfig(String appId, String pageSlug, String title, String subtitle,
                              PurchaseLayoutType layoutType, String themeJson, String contentJson) {
        this.appId = appId;
        this.pageSlug = pageSlug;
        this.title = title;
        this.subtitle = subtitle;
        this.layoutType = layoutType;
        this.themeJson = themeJson;
        this.contentJson = contentJson;
    }

    public Long getId() { return id; }
    public String getAppId() { return appId; }
    public String getPageSlug() { return pageSlug; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public PurchaseLayoutType getLayoutType() { return layoutType; }
    public String getThemeJson() { return themeJson; }
    public String getContentJson() { return contentJson; }
    public PurchasePageStatus getStatus() { return status; }

    public void update(String title, String subtitle, PurchaseLayoutType layoutType, String themeJson, String contentJson) {
        this.title = title;
        this.subtitle = subtitle;
        this.layoutType = layoutType;
        this.themeJson = themeJson;
        this.contentJson = contentJson;
    }

    public void changeStatus(PurchasePageStatus status) {
        this.status = status;
    }
}
