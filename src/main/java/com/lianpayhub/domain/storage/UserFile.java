package com.lianpayhub.domain.storage;

import com.lianpayhub.domain.BaseEntity;
import com.lianpayhub.service.storage.FileCategory;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_file", indexes = {
        @Index(name = "idx_user_file_owner", columnList = "user_id, app_id"),
        @Index(name = "idx_user_file_path", columnList = "user_id, app_id, virtual_path"),
        @Index(name = "idx_user_file_updated", columnList = "user_id, app_id, updated_at")
})
public class UserFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    /** 用户视角虚拟路径，如 /settings/config.json */
    @Column(name = "virtual_path", nullable = false, length = 1024)
    private String virtualPath;

    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;

    /** 实际存储 key，格式：sync/{appId}/{userId}/{UUID}.{ext} */
    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** SHA-256 校验和，用于去重和完整性验证 */
    @Column(name = "checksum", length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", nullable = false, length = 16)
    private FileCategory fileCategory;

    /** 乐观并发版本号，每次覆盖 +1 */
    @Column(name = "version", nullable = false)
    private long version = 1L;

    /** 非 null 表示已删除（保留记录供增量同步感知删除事件） */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected UserFile() {
    }

    public UserFile(Long userId, String appId, String virtualPath, String fileName,
                    String storageKey, String contentType, long sizeBytes,
                    String checksum, FileCategory fileCategory) {
        this.userId = userId;
        this.appId = appId;
        this.virtualPath = virtualPath;
        this.fileName = fileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.fileCategory = fileCategory;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void overwrite(String storageKey, String contentType, long sizeBytes, String checksum) {
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.version = this.version + 1;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public String getVirtualPath() { return virtualPath; }
    public String getFileName() { return fileName; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getChecksum() { return checksum; }
    public FileCategory getFileCategory() { return fileCategory; }
    public long getVersion() { return version; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
