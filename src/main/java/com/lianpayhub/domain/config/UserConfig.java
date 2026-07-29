package com.lianpayhub.domain.config;

import com.lianpayhub.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "user_config", indexes = {
        @Index(name = "idx_user_config_owner", columnList = "user_id,app_id"),
        @Index(name = "idx_user_config_version", columnList = "user_id,app_id,version")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_config_owner_key", columnNames = {"user_id", "app_id", "config_key"})
})
public class UserConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType = "text/plain";

    @Lob
    @Column(name = "content_text", nullable = false)
    private String contentText;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private Long version = 1L;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes = 0L;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected UserConfig() {
    }

    public UserConfig(Long userId, String appId, String configKey, String contentType,
                      String contentText, String contentHash, Long sizeBytes) {
        this.userId = userId;
        this.appId = appId;
        this.configKey = configKey;
        update(contentType, contentText, contentHash, sizeBytes);
        this.version = 1L;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public String getConfigKey() { return configKey; }
    public String getContentType() { return contentType; }
    public String getContentText() { return contentText; }
    public String getContentHash() { return contentHash; }
    public Long getVersion() { return version; }
    public Long getSizeBytes() { return sizeBytes; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public boolean isDeleted() { return deletedAt != null; }

    public void update(String contentType, String contentText, String contentHash, Long sizeBytes) {
        this.contentType = contentType == null || contentType.trim().isEmpty() ? "text/plain" : contentType.trim();
        this.contentText = contentText;
        this.contentHash = contentHash;
        this.sizeBytes = sizeBytes == null ? 0L : sizeBytes;
        this.version = this.version == null ? 1L : this.version + 1L;
        this.deletedAt = null;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.version = this.version == null ? 1L : this.version + 1L;
    }

    public void overrideVersion(Long version) {
        this.version = version == null ? 1L : version;
    }
}
