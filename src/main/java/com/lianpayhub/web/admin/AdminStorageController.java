package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.config.StorageProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/storage")
public class AdminStorageController {

    private final StorageProperties storageProperties;

    public AdminStorageController(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @GetMapping
    public ApiResponse<StorageConfigResult> current() {
        Path resolved = Paths.get(storageProperties.getLocalPath()).toAbsolutePath().normalize();
        return ApiResponse.ok(new StorageConfigResult(
                storageProperties.getBackend(),
                storageProperties.getLocalPath(),
                resolved.toString(),
                storageProperties.getLocalBaseUrl(),
                storageProperties.getMaxConfigFileBytes(),
                storageProperties.getMaxImageFileBytes(),
                storageProperties.getDefaultQuotaBytes(),
                storageProperties.getMaxFileCount()
        ));
    }

    public static class StorageConfigResult {
        private final String backend;
        private final String localPath;
        private final String resolvedLocalPath;
        private final String localBaseUrl;
        private final long maxConfigFileBytes;
        private final long maxImageFileBytes;
        private final long defaultQuotaBytes;
        private final int maxFileCount;

        public StorageConfigResult(String backend, String localPath, String resolvedLocalPath, String localBaseUrl,
                                   long maxConfigFileBytes, long maxImageFileBytes, long defaultQuotaBytes,
                                   int maxFileCount) {
            this.backend = backend;
            this.localPath = localPath;
            this.resolvedLocalPath = resolvedLocalPath;
            this.localBaseUrl = localBaseUrl;
            this.maxConfigFileBytes = maxConfigFileBytes;
            this.maxImageFileBytes = maxImageFileBytes;
            this.defaultQuotaBytes = defaultQuotaBytes;
            this.maxFileCount = maxFileCount;
        }

        public String getBackend() { return backend; }
        public String getLocalPath() { return localPath; }
        public String getResolvedLocalPath() { return resolvedLocalPath; }
        public String getLocalBaseUrl() { return localBaseUrl; }
        public long getMaxConfigFileBytes() { return maxConfigFileBytes; }
        public long getMaxImageFileBytes() { return maxImageFileBytes; }
        public long getDefaultQuotaBytes() { return defaultQuotaBytes; }
        public int getMaxFileCount() { return maxFileCount; }
    }
}
