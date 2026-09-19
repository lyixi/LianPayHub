package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "website_article", indexes = {@Index(name = "uk_website_article_slug", columnList = "slug", unique = true), @Index(name = "idx_website_article_category_status", columnList = "category,status")})
public class WebsiteArticle extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String slug;
    @Column(nullable = false, length = 48) private String category;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 512) private String excerpt;
    @Column(name = "cover_url", length = 512) private String coverUrl;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(name = "seo_title", length = 160) private String seoTitle;
    @Column(name = "seo_description", length = 320) private String seoDescription;
    @Column(name = "published_at") private LocalDateTime publishedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private ContentStatus status = ContentStatus.DRAFT;
    protected WebsiteArticle() {}
    public WebsiteArticle(String slug,String category,String title,String excerpt,String coverUrl,String content,String seoTitle,String seoDescription){this.slug=slug;this.category=category;this.title=title;this.excerpt=excerpt;this.coverUrl=coverUrl;this.content=content;this.seoTitle=seoTitle;this.seoDescription=seoDescription;}
    public Long getId(){return id;} public String getSlug(){return slug;} public String getCategory(){return category;} public String getTitle(){return title;} public String getExcerpt(){return excerpt;} public String getCoverUrl(){return coverUrl;} public String getContent(){return content;} public String getSeoTitle(){return seoTitle;} public String getSeoDescription(){return seoDescription;} public LocalDateTime getPublishedAt(){return publishedAt;} public ContentStatus getStatus(){return status;}
    public void update(String category,String title,String excerpt,String coverUrl,String content,String seoTitle,String seoDescription){this.category=category;this.title=title;this.excerpt=excerpt;this.coverUrl=coverUrl;this.content=content;this.seoTitle=seoTitle;this.seoDescription=seoDescription;}
    public void changeStatus(ContentStatus status){this.status=status;if(status==ContentStatus.PUBLISHED && publishedAt==null)publishedAt=LocalDateTime.now();}
}
