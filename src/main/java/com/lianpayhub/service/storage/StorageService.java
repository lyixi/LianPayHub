package com.lianpayhub.service.storage;

import java.io.InputStream;
import java.time.Duration;

public interface StorageService {

    /**
     * 存储文件，返回元数据。key 由调用方传入（UUID 格式）。
     */
    StoredFile store(String key, InputStream data, long size, String contentType);

    /**
     * 生成限时下载 URI（本地实现返回直接访问路径，OSS 实现返回签名 URL）。
     */
    String getDownloadUrl(String key, Duration ttl);

    /**
     * 物理删除文件。
     */
    void delete(String key);
}
