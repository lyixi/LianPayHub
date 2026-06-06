package com.lianpayhub.service.cloudsync;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.StorageProperties;
import com.lianpayhub.domain.storage.UserFile;
import com.lianpayhub.domain.storage.UserStorageQuota;
import com.lianpayhub.domain.storage.UserStorageQuotaId;
import com.lianpayhub.repository.UserFileRepository;
import com.lianpayhub.repository.UserStorageQuotaRepository;
import com.lianpayhub.service.storage.AllowedFileType;
import com.lianpayhub.service.storage.FileValidator;
import com.lianpayhub.service.storage.StorageService;
import com.lianpayhub.service.storage.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CloudSyncService {

    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(15);

    private final UserFileRepository fileRepo;
    private final UserStorageQuotaRepository quotaRepo;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final StorageProperties props;

    // 简单内存速率限制：key = "userId:appId"，value = 本分钟上传次数
    // 生产环境可替换为 Redis
    private final Map<String, RateBucket> rateBuckets = new ConcurrentHashMap<>();

    public CloudSyncService(UserFileRepository fileRepo,
                            UserStorageQuotaRepository quotaRepo,
                            StorageService storageService,
                            FileValidator fileValidator,
                            StorageProperties props) {
        this.fileRepo = fileRepo;
        this.quotaRepo = quotaRepo;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
        this.props = props;
    }

    @Transactional
    public UserFile upload(UploadCommand cmd) {
        checkUploadRateLimit(cmd.getUserId(), cmd.getAppId());

        AllowedFileType type = fileValidator.validate(cmd.getFile());
        String normalizedPath = fileValidator.validateVirtualPath(cmd.getVirtualPath());
        String fileName = extractFileName(normalizedPath);

        long fileSize = cmd.getFile().getSize();
        UserStorageQuota quota = getOrCreateQuota(cmd.getUserId(), cmd.getAppId());

        // 检查文件数量上限（仅对新文件；覆盖写入不新增数量）
        Optional<UserFile> existing = fileRepo.findByUserIdAndAppIdAndVirtualPath(
                cmd.getUserId(), cmd.getAppId(), normalizedPath);
        boolean isOverwrite = existing.isPresent() && !existing.get().isDeleted();

        if (!isOverwrite) {
            if (quota.getFileCount() >= props.getMaxFileCount()) {
                throw new BusinessException(ErrorCode.CONFLICT,
                        "文件数量已达上限：" + props.getMaxFileCount());
            }
            if (quota.getUsedBytes() + fileSize > quota.getQuotaBytes()) {
                throw new BusinessException(ErrorCode.CONFLICT,
                        "存储空间不足，剩余：" + formatBytes(quota.getQuotaBytes() - quota.getUsedBytes()));
            }
        }

        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
        String storageKey = "sync/" + cmd.getAppId() + "/" + cmd.getUserId() + "/" + UUID.randomUUID() + ext;
        String checksum = computeChecksum(cmd);

        try {
            storageService.store(storageKey, cmd.getFile().getInputStream(), fileSize, type.getMimeType());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "文件存储失败");
        }

        UserFile userFile;
        if (isOverwrite) {
            userFile = existing.get();
            long oldSize = userFile.getSizeBytes();
            String oldKey = userFile.getStorageKey();
            userFile.overwrite(storageKey, type.getMimeType(), fileSize, checksum);
            // 覆盖：先删旧文件，再原子调整配额差值
            storageService.delete(oldKey);
            long delta = fileSize - oldSize;
            if (delta > 0) {
                quotaRepo.incrementUsage(cmd.getUserId(), cmd.getAppId(), delta, 0);
            } else if (delta < 0) {
                quotaRepo.decrementUsage(cmd.getUserId(), cmd.getAppId(), -delta, 0);
            }
        } else {
            userFile = new UserFile(cmd.getUserId(), cmd.getAppId(), normalizedPath, fileName,
                    storageKey, type.getMimeType(), fileSize, checksum, type.getCategory());
            fileRepo.save(userFile);
            quotaRepo.incrementUsage(cmd.getUserId(), cmd.getAppId(), fileSize, 1);
        }

        return userFile;
    }

    @Transactional(readOnly = true)
    public List<UserFile> listDirectory(Long userId, String appId, String path) {
        String normalizedPath = fileValidator.validateVirtualPath(path);
        // 匹配直接子项：路径以 normalizedPath/ 开头，且后续不含 /
        String prefix = normalizedPath.endsWith("/") ? normalizedPath : normalizedPath + "/";
        return fileRepo.findByDirectory(userId, appId, prefix + "%");
    }

    @Transactional(readOnly = true)
    public String getDownloadUrl(Long userId, String appId, Long fileId) {
        UserFile file = loadOwnedFile(userId, appId, fileId);
        if (file.isDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return storageService.getDownloadUrl(file.getStorageKey(), DOWNLOAD_URL_TTL);
    }

    @Transactional
    public void deleteFile(Long userId, String appId, Long fileId) {
        UserFile file = loadOwnedFile(userId, appId, fileId);
        if (file.isDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        file.softDelete();
        quotaRepo.decrementUsage(userId, appId, file.getSizeBytes(), 1);
    }

    @Transactional(readOnly = true)
    public List<UserFile> getChangesSince(Long userId, String appId, LocalDateTime since) {
        return fileRepo.findChangedSince(userId, appId, since);
    }

    // ── 私有辅助方法 ──────────────────────────────────────────────────────────

    private UserFile loadOwnedFile(Long userId, String appId, Long fileId) {
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "文件不存在"));
        if (!file.getUserId().equals(userId) || !file.getAppId().equals(appId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该文件");
        }
        return file;
    }

    private UserStorageQuota getOrCreateQuota(Long userId, String appId) {
        return quotaRepo.findById(new UserStorageQuotaId(userId, appId))
                .orElseGet(() -> quotaRepo.save(
                        new UserStorageQuota(userId, appId, props.getDefaultQuotaBytes())));
    }

    private void checkUploadRateLimit(Long userId, String appId) {
        String key = userId + ":" + appId;
        RateBucket bucket = rateBuckets.compute(key, (k, b) -> {
            long now = System.currentTimeMillis() / 60000L;
            if (b == null || b.minute != now) {
                return new RateBucket(now);
            }
            return b;
        });
        if (bucket.count.incrementAndGet() > props.getUploadRateLimitPerMinute()) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "上传过于频繁，请稍后再试");
        }
    }

    private String extractFileName(String virtualPath) {
        int slash = virtualPath.lastIndexOf('/');
        String name = slash >= 0 ? virtualPath.substring(slash + 1) : virtualPath;
        if (name.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "路径末尾不能为目录");
        }
        return name;
    }

    private String computeChecksum(UploadCommand cmd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = cmd.getFile().getBytes();
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        if (bytes >= 1024) return (bytes / 1024) + " KB";
        return bytes + " B";
    }

    private static class RateBucket {
        final long minute;
        final AtomicInteger count = new AtomicInteger(0);

        RateBucket(long minute) {
            this.minute = minute;
        }
    }
}
