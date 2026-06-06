package com.lianpayhub.web.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "增量同步结果")
public class FileChangesResult {

    @Schema(description = "变更列表，含软删除条目（deleted=true）")
    private final List<FileInfoResult> changes;

    @Schema(description = "本次同步时间戳（毫秒），下次调用 /changes 时作为 since 参数使用")
    private final long syncTimestamp;

    public FileChangesResult(List<FileInfoResult> changes, long syncTimestamp) {
        this.changes = changes;
        this.syncTimestamp = syncTimestamp;
    }

    public List<FileInfoResult> getChanges() { return changes; }
    public long getSyncTimestamp() { return syncTimestamp; }
}
