package com.lianpayhub.web.api;

import com.lianpayhub.domain.storage.UserFile;
import com.lianpayhub.service.storage.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "文件元数据")
public class FileInfoResult {

    @Schema(description = "文件 ID")
    private final Long id;

    @Schema(description = "虚拟路径，如 /settings/config.json")
    private final String virtualPath;

    @Schema(description = "文件名")
    private final String fileName;

    @Schema(description = "MIME 类型")
    private final String contentType;

    @Schema(description = "文件字节数")
    private final long sizeBytes;

    @Schema(description = "SHA-256 校验和")
    private final String checksum;

    @Schema(description = "文件分类：CONFIG / IMAGE")
    private final FileCategory fileCategory;

    @Schema(description = "版本号，覆盖写入时自增")
    private final long version;

    @Schema(description = "是否已删除（软删除）")
    private final boolean deleted;

    @Schema(description = "创建时间")
    private final LocalDateTime createdAt;

    @Schema(description = "最后更新时间（含软删除操作）")
    private final LocalDateTime updatedAt;

    public FileInfoResult(UserFile f) {
        this.id = f.getId();
        this.virtualPath = f.getVirtualPath();
        this.fileName = f.getFileName();
        this.contentType = f.getContentType();
        this.sizeBytes = f.getSizeBytes();
        this.checksum = f.getChecksum();
        this.fileCategory = f.getFileCategory();
        this.version = f.getVersion();
        this.deleted = f.isDeleted();
        this.createdAt = f.getCreatedAt();
        this.updatedAt = f.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getVirtualPath() { return virtualPath; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getChecksum() { return checksum; }
    public FileCategory getFileCategory() { return fileCategory; }
    public long getVersion() { return version; }
    public boolean isDeleted() { return deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
