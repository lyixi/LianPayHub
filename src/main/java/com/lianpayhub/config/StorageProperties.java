package com.lianpayhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.storage")
public class StorageProperties {

    /** 存储后端：local / aliyun-oss */
    private String backend = "local";

    /** 本地存储根目录 */
    private String localPath = "./storage";

    /** 本地文件访问的 URL 前缀（用于生成下载链接） */
    private String localBaseUrl = "http://localhost:8888/files";

    /** 单个配置文件最大字节数（默认 512 KB） */
    private long maxConfigFileBytes = 524288L;

    /** 单个图片最大字节数（默认 5 MB） */
    private long maxImageFileBytes = 5242880L;

    /** 每个 (user, app) 的默认存储配额字节数（默认 50 MB） */
    private long defaultQuotaBytes = 52428800L;

    /** 每个 (user, app) 的最大文件数量 */
    private int maxFileCount = 500;

    /** 虚拟路径最大层级深度 */
    private int maxPathDepth = 8;

    /** 每用户每分钟上传次数上限 */
    private int uploadRateLimitPerMinute = 20;

    public String getBackend() { return backend; }
    public void setBackend(String backend) { this.backend = backend; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public String getLocalBaseUrl() { return localBaseUrl; }
    public void setLocalBaseUrl(String localBaseUrl) { this.localBaseUrl = localBaseUrl; }

    public long getMaxConfigFileBytes() { return maxConfigFileBytes; }
    public void setMaxConfigFileBytes(long maxConfigFileBytes) { this.maxConfigFileBytes = maxConfigFileBytes; }

    public long getMaxImageFileBytes() { return maxImageFileBytes; }
    public void setMaxImageFileBytes(long maxImageFileBytes) { this.maxImageFileBytes = maxImageFileBytes; }

    public long getDefaultQuotaBytes() { return defaultQuotaBytes; }
    public void setDefaultQuotaBytes(long defaultQuotaBytes) { this.defaultQuotaBytes = defaultQuotaBytes; }

    public int getMaxFileCount() { return maxFileCount; }
    public void setMaxFileCount(int maxFileCount) { this.maxFileCount = maxFileCount; }

    public int getMaxPathDepth() { return maxPathDepth; }
    public void setMaxPathDepth(int maxPathDepth) { this.maxPathDepth = maxPathDepth; }

    public int getUploadRateLimitPerMinute() { return uploadRateLimitPerMinute; }
    public void setUploadRateLimitPerMinute(int uploadRateLimitPerMinute) {
        this.uploadRateLimitPerMinute = uploadRateLimitPerMinute;
    }
}
