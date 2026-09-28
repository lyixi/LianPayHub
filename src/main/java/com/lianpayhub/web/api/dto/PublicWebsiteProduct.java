package com.lianpayhub.web.api.dto;

import com.lianpayhub.domain.website.ContentStatus;
import com.lianpayhub.domain.website.WebsiteProduct;
import java.time.LocalDateTime;

/** Public website projection. Internal identifiers such as appId must never cross this boundary. */
public final class PublicWebsiteProduct {
    private final Long id;
    private final String slug;
    private final String name;
    private final String tagline;
    private final String summary;
    private final String accentColor;
    private final String coverUrl;
    private final String downloadUrl;
    private final String downloadJson;
    private final String purchaseUrl;
    private final String content;
    private final String mediaJson;
    private final String previewProject;
    private final String previewVolume;
    private final String previewChapter;
    private final String previewExcerpt;
    private final String previewInsight;
    private final String previewChapterTwo;
    private final String previewExcerptTwo;
    private final String previewChapterThree;
    private final String previewExcerptThree;
    private final String previewBody;
    private final String previewDraft;
    private final Integer sortOrder;
    private final ContentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private PublicWebsiteProduct(WebsiteProduct product) {
        this.id = product.getId();
        this.slug = product.getSlug();
        this.name = product.getName();
        this.tagline = product.getTagline();
        this.summary = product.getSummary();
        this.accentColor = product.getAccentColor();
        this.coverUrl = product.getCoverUrl();
        this.downloadUrl = product.getDownloadUrl();
        this.downloadJson = product.getDownloadJson();
        this.purchaseUrl = product.getPurchaseUrl();
        this.content = product.getContent();
        this.mediaJson = product.getMediaJson();
        this.previewProject = product.getPreviewProject();
        this.previewVolume = product.getPreviewVolume();
        this.previewChapter = product.getPreviewChapter();
        this.previewExcerpt = product.getPreviewExcerpt();
        this.previewInsight = product.getPreviewInsight();
        this.previewChapterTwo = product.getPreviewChapterTwo();
        this.previewExcerptTwo = product.getPreviewExcerptTwo();
        this.previewChapterThree = product.getPreviewChapterThree();
        this.previewExcerptThree = product.getPreviewExcerptThree();
        this.previewBody = product.getPreviewBody();
        this.previewDraft = product.getPreviewDraft();
        this.sortOrder = product.getSortOrder();
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public static PublicWebsiteProduct from(WebsiteProduct product) { return new PublicWebsiteProduct(product); }
    public Long getId(){return id;} public String getSlug(){return slug;} public String getName(){return name;}
    public String getTagline(){return tagline;} public String getSummary(){return summary;} public String getAccentColor(){return accentColor;}
    public String getCoverUrl(){return coverUrl;} public String getDownloadUrl(){return downloadUrl;} public String getDownloadJson(){return downloadJson;}
    public String getPurchaseUrl(){return purchaseUrl;} public String getContent(){return content;} public String getMediaJson(){return mediaJson;}
    public String getPreviewProject(){return previewProject;} public String getPreviewVolume(){return previewVolume;}
    public String getPreviewChapter(){return previewChapter;} public String getPreviewExcerpt(){return previewExcerpt;} public String getPreviewInsight(){return previewInsight;}
    public String getPreviewChapterTwo(){return previewChapterTwo;} public String getPreviewExcerptTwo(){return previewExcerptTwo;}
    public String getPreviewChapterThree(){return previewChapterThree;} public String getPreviewExcerptThree(){return previewExcerptThree;}
    public String getPreviewBody(){return previewBody;} public String getPreviewDraft(){return previewDraft;}
    public Integer getSortOrder(){return sortOrder;} public ContentStatus getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
