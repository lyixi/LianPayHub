package com.lianpayhub.web.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "文件下载 URL（限时有效）")
public class FileDownloadUrlResult {

    @Schema(description = "下载地址（本地模式为直接 URL，OSS 模式为签名临时 URL）")
    private final String url;

    @Schema(description = "有效期，单位秒")
    private final long expiresInSeconds;

    public FileDownloadUrlResult(String url, long expiresInSeconds) {
        this.url = url;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getUrl() { return url; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
}
