package com.lianpayhub.service.storage;

import com.lianpayhub.config.StorageProperties;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "lianpayhub.storage.backend", havingValue = "local", matchIfMissing = true)
public class LocalDiskStorageService implements StorageService {

    private final StorageProperties props;
    private Path rootPath;

    public LocalDiskStorageService(StorageProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() throws IOException {
        rootPath = Paths.get(props.getLocalPath()).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    @Override
    public StoredFile store(String key, InputStream data, long size, String contentType) {
        Path target = resolveKey(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "文件写入失败");
        }
        return new StoredFile(key, size, contentType);
    }

    @Override
    public String getDownloadUrl(String key, Duration ttl) {
        // 本地实现不做签名，直接拼接 URL；生产环境替换为 OSS 签名实现
        return props.getLocalBaseUrl() + "/" + key;
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolveKey(key));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "文件删除失败");
        }
    }

    private Path resolveKey(String key) {
        // 防止 key 包含路径穿越字符（二次防护，实际 key 由服务端生成）
        Path resolved = rootPath.resolve(key).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
        return resolved;
    }
}
