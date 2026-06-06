package com.lianpayhub.service.cloudsync;

import org.springframework.web.multipart.MultipartFile;

public class UploadCommand {
    private final Long userId;
    private final String appId;
    private final String virtualPath;  // 目标虚拟路径，如 /settings/config.json
    private final MultipartFile file;

    public UploadCommand(Long userId, String appId, String virtualPath, MultipartFile file) {
        this.userId = userId;
        this.appId = appId;
        this.virtualPath = virtualPath;
        this.file = file;
    }

    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }
    public String getVirtualPath() { return virtualPath; }
    public MultipartFile getFile() { return file; }
}
