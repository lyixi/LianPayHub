package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "website_page", indexes = {@Index(name = "uk_website_page_slug", columnList = "slug", unique = true)})
public class WebsitePage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String slug;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 512) private String excerpt;
    @Column(name = "cover_url", length = 512) private String coverUrl;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(name = "seo_title", length = 160) private String seoTitle;
    @Column(name = "seo_description", length = 320) private String seoDescription;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private ContentStatus status = ContentStatus.DRAFT;
    protected WebsitePage() {}
    public WebsitePage(String slug,String title,String excerpt,String coverUrl,String content,String seoTitle,String seoDescription,Integer sortOrder){this.slug=slug;this.title=title;this.excerpt=excerpt;this.coverUrl=coverUrl;this.content=content;this.seoTitle=seoTitle;this.seoDescription=seoDescription;this.sortOrder=sortOrder==null?0:sortOrder;}
    public Long getId(){return id;} public String getSlug(){return slug;} public String getTitle(){return title;} public String getExcerpt(){return excerpt;} public String getCoverUrl(){return coverUrl;} public String getContent(){return content;} public String getSeoTitle(){return seoTitle;} public String getSeoDescription(){return seoDescription;} public Integer getSortOrder(){return sortOrder;} public ContentStatus getStatus(){return status;}
    public void update(String title,String excerpt,String coverUrl,String content,String seoTitle,String seoDescription,Integer sortOrder){this.title=title;this.excerpt=excerpt;this.coverUrl=coverUrl;this.content=content;this.seoTitle=seoTitle;this.seoDescription=seoDescription;this.sortOrder=sortOrder==null?0:sortOrder;}
    public void changeStatus(ContentStatus status){this.status=status;}
}
