package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "website_product", indexes = {@Index(name = "uk_website_product_slug", columnList = "slug", unique = true)})
public class WebsiteProduct extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String slug;
    @Column(name = "app_id", length = 64) private String appId;
    @Column(nullable = false, length = 128) private String name;
    @Column(nullable = false, length = 128) private String tagline;
    @Column(length = 512) private String summary;
    @Column(name = "accent_color", length = 16) private String accentColor;
    @Column(name = "cover_url", length = 512) private String coverUrl;
    @Column(name = "download_url", length = 512) private String downloadUrl;
    @Column(name = "purchase_url", length = 512) private String purchaseUrl;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(name = "media_json", columnDefinition = "TEXT") private String mediaJson;
    @Column(name = "preview_project", length = 128) private String previewProject;
    @Column(name = "preview_volume", length = 128) private String previewVolume;
    @Column(name = "preview_chapter", length = 128) private String previewChapter;
    @Column(name = "preview_excerpt", length = 600) private String previewExcerpt;
    @Column(name = "preview_insight", length = 600) private String previewInsight;
    @Column(name = "preview_chapter_two", length = 128) private String previewChapterTwo;
    @Column(name = "preview_excerpt_two", length = 600) private String previewExcerptTwo;
    @Column(name = "preview_chapter_three", length = 128) private String previewChapterThree;
    @Column(name = "preview_excerpt_three", length = 600) private String previewExcerptThree;
    @Column(name = "preview_body", length = 800) private String previewBody;
    @Column(name = "preview_draft", length = 600) private String previewDraft;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private ContentStatus status = ContentStatus.DRAFT;
    protected WebsiteProduct() {}
    public WebsiteProduct(String slug, String appId, String name, String tagline, String summary, String accentColor, String coverUrl, String downloadUrl, String purchaseUrl, String content, Integer sortOrder) { this.slug=slug; this.appId=appId; this.name=name; this.tagline=tagline; this.summary=summary; this.accentColor=accentColor; this.coverUrl=coverUrl; this.downloadUrl=downloadUrl; this.purchaseUrl=purchaseUrl; this.content=content; this.sortOrder=sortOrder == null ? 0 : sortOrder; }
    public Long getId(){return id;} public String getSlug(){return slug;} public String getAppId(){return appId;} public String getName(){return name;} public String getTagline(){return tagline;} public String getSummary(){return summary;} public String getAccentColor(){return accentColor;} public String getCoverUrl(){return coverUrl;} public String getDownloadUrl(){return downloadUrl;} public String getPurchaseUrl(){return purchaseUrl;} public String getContent(){return content;} public String getMediaJson(){return mediaJson;} public Integer getSortOrder(){return sortOrder;} public ContentStatus getStatus(){return status;}
    public void update(String appId,String name,String tagline,String summary,String accentColor,String coverUrl,String downloadUrl,String purchaseUrl,String content,String mediaJson,Integer sortOrder){this.appId=appId;this.name=name;this.tagline=tagline;this.summary=summary;this.accentColor=accentColor;this.coverUrl=coverUrl;this.downloadUrl=downloadUrl;this.purchaseUrl=purchaseUrl;this.content=content;this.mediaJson=mediaJson;this.sortOrder=sortOrder == null?0:sortOrder;}
    public void updatePreview(String project,String volume,String chapter,String excerpt,String insight){this.previewProject=project;this.previewVolume=volume;this.previewChapter=chapter;this.previewExcerpt=excerpt;this.previewInsight=insight;}
    public void updatePreviewDetails(String chapterTwo,String excerptTwo,String chapterThree,String excerptThree,String body,String draft){this.previewChapterTwo=chapterTwo;this.previewExcerptTwo=excerptTwo;this.previewChapterThree=chapterThree;this.previewExcerptThree=excerptThree;this.previewBody=body;this.previewDraft=draft;}
    public void initializePreviewDefaults(String project,String volume,String chapter,String excerpt,String insight,String chapterTwo,String excerptTwo,String chapterThree,String excerptThree,String body,String draft){
        if(this.previewProject==null)this.previewProject=project;if(this.previewVolume==null)this.previewVolume=volume;
        if(this.previewChapter==null)this.previewChapter=chapter;if(this.previewExcerpt==null)this.previewExcerpt=excerpt;
        if(this.previewInsight==null)this.previewInsight=insight;if(this.previewChapterTwo==null)this.previewChapterTwo=chapterTwo;
        if(this.previewExcerptTwo==null)this.previewExcerptTwo=excerptTwo;if(this.previewChapterThree==null)this.previewChapterThree=chapterThree;
        if(this.previewExcerptThree==null)this.previewExcerptThree=excerptThree;if(this.previewBody==null)this.previewBody=body;
        if(this.previewDraft==null)this.previewDraft=draft;
    }
    public String getPreviewProject(){return previewProject;} public String getPreviewVolume(){return previewVolume;} public String getPreviewChapter(){return previewChapter;} public String getPreviewExcerpt(){return previewExcerpt;} public String getPreviewInsight(){return previewInsight;}
    public String getPreviewChapterTwo(){return previewChapterTwo;} public String getPreviewExcerptTwo(){return previewExcerptTwo;}
    public String getPreviewChapterThree(){return previewChapterThree;} public String getPreviewExcerptThree(){return previewExcerptThree;}
    public String getPreviewBody(){return previewBody;} public String getPreviewDraft(){return previewDraft;}
    public void changeStatus(ContentStatus status){this.status=status;}
}
