package com.lianpayhub.domain.storage;

import javax.persistence.*;

/**
 * 每个 (user, app) 独立的存储配额记录。
 * usedBytes 和 fileCount 通过原子 UPDATE 维护，避免竞态条件。
 */
@Entity
@Table(name = "user_storage_quota")
public class UserStorageQuota {

    @EmbeddedId
    private UserStorageQuotaId id;

    @Column(name = "used_bytes", nullable = false)
    private long usedBytes = 0L;

    @Column(name = "quota_bytes", nullable = false)
    private long quotaBytes;

    @Column(name = "file_count", nullable = false)
    private int fileCount = 0;

    protected UserStorageQuota() {
    }

    public UserStorageQuota(Long userId, String appId, long quotaBytes) {
        this.id = new UserStorageQuotaId(userId, appId);
        this.quotaBytes = quotaBytes;
    }

    public UserStorageQuotaId getId() { return id; }
    public long getUsedBytes() { return usedBytes; }
    public long getQuotaBytes() { return quotaBytes; }
    public int getFileCount() { return fileCount; }
}
