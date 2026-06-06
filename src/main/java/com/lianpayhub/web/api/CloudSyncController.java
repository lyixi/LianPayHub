package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.storage.UserFile;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.cloudsync.CloudSyncService;
import com.lianpayhub.service.cloudsync.UploadCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "云同步", description = "用户配置文件云同步接口（按账号+App 隔离）。所有接口需携带 App 用户 JWT。")
@RestController
@RequestMapping("/api/sync")
public class CloudSyncController {

    private static final long DOWNLOAD_URL_TTL_SECONDS = 900L;

    private final CloudSyncService syncService;

    public CloudSyncController(CloudSyncService syncService) {
        this.syncService = syncService;
    }

    @Operation(
        summary = "上传文件",
        description = "上传配置文件或图片到指定虚拟路径。同路径再次上传为覆盖写入，version 字段自增。" +
                      "支持类型：.json .yml .yaml .toml .xml .ini .conf .properties .txt .jpg .png .gif .webp。" +
                      "配置文件上限 512 KB，图片上限 5 MB。"
    )
    @PostMapping("/upload")
    public ApiResponse<FileInfoResult> upload(
            @Parameter(description = "文件内容（multipart）") @RequestParam("file") MultipartFile file,
            @Parameter(description = "目标虚拟路径，如 /settings/config.json") @RequestParam("path") String path,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        UploadCommand cmd = new UploadCommand(
                principal.getUserId(), principal.getAppId(), path, file);
        UserFile result = syncService.upload(cmd);
        return ApiResponse.ok(new FileInfoResult(result));
    }

    @Operation(
        summary = "列出目录",
        description = "列出指定路径下的所有活跃文件（不含已软删除文件）。path 默认为根目录 /。"
    )
    @GetMapping("/list")
    public ApiResponse<List<FileInfoResult>> list(
            @Parameter(description = "目录路径，默认 /") @RequestParam(value = "path", defaultValue = "/") String path,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        List<UserFile> files = syncService.listDirectory(
                principal.getUserId(), principal.getAppId(), path);
        List<FileInfoResult> results = files.stream()
                .map(FileInfoResult::new)
                .collect(Collectors.toList());
        return ApiResponse.ok(results);
    }

    @Operation(
        summary = "获取下载 URL",
        description = "返回指定文件的限时下载地址（15 分钟有效）。每次请求重新签名，不缓存。"
    )
    @GetMapping("/{fileId}/url")
    public ApiResponse<FileDownloadUrlResult> getDownloadUrl(
            @Parameter(description = "文件 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        String url = syncService.getDownloadUrl(
                principal.getUserId(), principal.getAppId(), fileId);
        return ApiResponse.ok(new FileDownloadUrlResult(url, DOWNLOAD_URL_TTL_SECONDS));
    }

    @Operation(
        summary = "删除文件",
        description = "软删除指定文件。记录保留，deleted 字段置为 true，供增量同步感知删除事件。"
    )
    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "文件 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        syncService.deleteFile(principal.getUserId(), principal.getAppId(), fileId);
        return ApiResponse.ok();
    }

    @Operation(
        summary = "增量同步",
        description = "返回 since 时间戳之后的所有变更记录，含软删除条目（deleted=true）。\n\n" +
                      "**同步协议：**\n" +
                      "1. 首次同步传 `since=0`，获取全量文件列表\n" +
                      "2. 保存响应中的 `syncTimestamp`\n" +
                      "3. 后续同步传上次的 `syncTimestamp` 作为 `since`\n" +
                      "4. 对 `deleted=true` 的条目，在本地删除对应文件"
    )
    @GetMapping("/changes")
    public ApiResponse<FileChangesResult> getChanges(
            @Parameter(description = "上次同步时间戳（毫秒），首次传 0") @RequestParam(value = "since", defaultValue = "0") long sinceMillis,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        LocalDateTime since = sinceMillis == 0
                ? LocalDateTime.of(2000, 1, 1, 0, 0)
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(sinceMillis), ZoneOffset.UTC);

        List<FileInfoResult> changes = syncService
                .getChangesSince(principal.getUserId(), principal.getAppId(), since)
                .stream()
                .map(FileInfoResult::new)
                .collect(Collectors.toList());

        long now = Instant.now().toEpochMilli();
        return ApiResponse.ok(new FileChangesResult(changes, now));
    }
}
