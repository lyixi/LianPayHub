package com.lianpayhub.domain.website;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "website_media", indexes = {@Index(name = "uk_website_media_storage_key", columnList = "storage_key", unique = true)})
public class WebsiteMedia extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "storage_key", nullable = false, length = 512) private String storageKey;
    @Column(nullable = false, length = 512) private String url;
    @Column(name = "file_name", nullable = false, length = 255) private String fileName;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "size_bytes", nullable = false) private Long sizeBytes;
    @Column(length = 180) private String title;
    @Column(name = "alt_text", length = 255) private String altText;
    @Column(length = 1000) private String description;

    protected WebsiteMedia() {}
    public WebsiteMedia(String storageKey,String url,String fileName,String contentType,Long sizeBytes,String title,String altText,String description){this.storageKey=storageKey;this.url=url;this.fileName=fileName;this.contentType=contentType;this.sizeBytes=sizeBytes;this.title=title;this.altText=altText;this.description=description;}
    public Long getId(){return id;} public String getStorageKey(){return storageKey;} public String getUrl(){return url;} public String getFileName(){return fileName;} public String getContentType(){return contentType;} public Long getSizeBytes(){return sizeBytes;} public String getTitle(){return title;} public String getAltText(){return altText;} public String getDescription(){return description;}
    public void update(String title,String altText,String description){this.title=title;this.altText=altText;this.description=description;}
}
