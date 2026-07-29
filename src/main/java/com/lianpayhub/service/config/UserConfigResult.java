package com.lianpayhub.service.config;

import com.lianpayhub.domain.config.UserConfig;
import java.time.LocalDateTime;

public class UserConfigResult {
    private final String key;
    private final String contentType;
    private final String contentText;
    private final String contentHash;
    private final Long version;
    private final Long sizeBytes;
    private final boolean deleted;
    private final LocalDateTime updatedAt;

    public UserConfigResult(UserConfig config) {
        this.key = config.getConfigKey();
        this.contentType = config.getContentType();
        this.contentText = config.getContentText();
        this.contentHash = config.getContentHash();
        this.version = config.getVersion();
        this.sizeBytes = config.getSizeBytes();
        this.deleted = config.isDeleted();
        this.updatedAt = config.getUpdatedAt();
    }

    public String getKey() { return key; }
    public String getContentType() { return contentType; }
    public String getContentText() { return contentText; }
    public String getContentHash() { return contentHash; }
    public Long getVersion() { return version; }
    public Long getSizeBytes() { return sizeBytes; }
    public boolean isDeleted() { return deleted; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
