# 云同步文件系统设计文档

## 概述

为每个 (用户, App) 提供独立的配置文件云同步空间。主要用于同步配置文件（JSON/YAML/XML 等），兼支持图片（PNG/JPG/WebP/GIF）。通过白名单文件类型、多层配额限制和原子操作确保安全性和数据一致性。

---

## 架构

```
客户端
  │
  └─ POST /api/sync/upload          上传文件（JWT 鉴权）
  └─ GET  /api/sync/list?path=/     列出目录
  └─ GET  /api/sync/{id}/url        获取限时下载 URL（15 分钟）
  └─ DELETE /api/sync/{id}          删除文件
  └─ GET  /api/sync/changes?since=  增量同步

CloudSyncController
  │
  └─ CloudSyncService               业务逻辑：校验 → 配额 → 存储 → 元数据
         │
         ├─ FileValidator           白名单 + Magic Bytes + ImageIO 验证
         ├─ StorageService          存储抽象接口
         │    └─ LocalDiskStorageService    开发环境（./storage/）
         │    └─ AliyunOssStorageService    生产环境（待实现）
         └─ UserFileRepository / UserStorageQuotaRepository
```

---

## 数据库

### user_file

文件元数据表，storage_key 指向实际存储位置，virtual_path 为用户视角路径。软删除（deleted_at 非空）记录保留，用于增量同步感知删除事件。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，雪花算法不可猜测 |
| user_id | BIGINT | 所属用户 |
| app_id | VARCHAR(64) | 所属 App（多租户隔离） |
| virtual_path | VARCHAR(1024) | 用户视角路径，如 `/settings/config.json` |
| storage_key | VARCHAR(512) | 实际存储 key，格式：`sync/{appId}/{userId}/{UUID}.{ext}` |
| size_bytes | BIGINT | 文件字节数 |
| checksum | VARCHAR(64) | SHA-256，用于完整性验证 |
| file_category | VARCHAR(16) | CONFIG / IMAGE |
| version | BIGINT | 乐观版本号，覆盖写入时递增 |
| deleted_at | DATETIME | 软删除时间，NULL 表示活跃 |

### user_storage_quota

每个 (user_id, app_id) 一条记录，used_bytes 和 file_count 通过原子 UPDATE 维护，避免竞态。

---

## 文件类型白名单

| 类型 | 扩展名 | 校验方式 |
|------|--------|----------|
| JSON | .json | 扩展名 |
| YAML | .yml .yaml | 扩展名 |
| TOML | .toml | 扩展名 |
| XML | .xml | 扩展名 |
| INI/Properties | .ini .conf .properties | 扩展名 |
| TXT | .txt | 扩展名 |
| JPEG | .jpg .jpeg | Magic Bytes `FF D8 FF` + ImageIO 解码 |
| PNG | .png | Magic Bytes `89 50 4E 47` + ImageIO 解码 |
| GIF | .gif | Magic Bytes `47 49 46 38` + ImageIO 解码 |
| WebP | .webp | Magic Bytes `52 49 46 46` (RIFF) + ImageIO 解码 |

**不允许**：SVG（可嵌入 JS）、ZIP/RAR（Zip Bomb）、可执行文件、脚本文件。

---

## 安全防护

| 威胁 | 防护措施 |
|------|----------|
| 上传恶意文件 | 白名单扩展名 + Magic Bytes 验证，两者必须匹配 |
| 图片炸弹 | `ImageIO.read()` 解码验证 + 宽高上限 4096px |
| 路径穿越 | `virtual_path` 服务端标准化，`storage_key` 由 UUID 生成，互不依赖 |
| 存储爆炸（单用户） | 每 (user, app)：50 MB 配额 + 500 文件数上限 |
| API 滥用上传 | 每用户每分钟 20 次上传限制（内存 RateBucket，生产可替换为 Redis） |
| 越权访问他人文件 | 所有查询强制双条件 `user_id AND app_id`，均从 JWT 取值 |
| 下载 URL 泄露 | 签名 URL TTL 15 分钟，本地模式下 UUID key 保证不可枚举 |
| 竞态导致超额写入 | 配额变更使用数据库原子 `UPDATE ... SET used = used + ?` |

---

## 配置项（application.yml）

```yaml
lianpayhub:
  storage:
    backend: local                  # local | aliyun-oss
    local-path: ./storage
    local-base-url: http://localhost:8888/files
    max-config-file-bytes: 524288   # 512 KB
    max-image-file-bytes: 5242880   # 5 MB
    default-quota-bytes: 52428800   # 50 MB per (user, app)
    max-file-count: 500
    max-path-depth: 8
    upload-rate-limit-per-minute: 20
```

---

## API 接口

所有接口需携带 `Authorization: Bearer {token}` 头（App 用户 JWT）。

### 上传文件
```
POST /api/sync/upload
Content-Type: multipart/form-data

file:   文件内容
path:   虚拟路径，如 /settings/config.json（同路径覆盖写入）
```

### 列出目录
```
GET /api/sync/list?path=/settings
```

### 获取下载 URL
```
GET /api/sync/{fileId}/url

Response:
{
  "url": "https://...",
  "expiresInSeconds": 900
}
```

### 删除文件
```
DELETE /api/sync/{fileId}
```

### 增量同步
```
GET /api/sync/changes?since=1748500000000

Response:
{
  "changes": [
    {
      "id": 123,
      "virtualPath": "/settings/config.json",
      "deleted": false,
      "version": 2,
      "updatedAt": "2026-01-01T10:00:00"
      // ...
    }
  ],
  "syncTimestamp": 1748503600000   // 下次 since 参数使用此值
}
```

**增量同步协议：**
客户端本地保存上次 `syncTimestamp`，每次调用 `/changes?since={syncTimestamp}`，拉取所有变更（含软删除记录），`deleted=true` 时本地删除对应文件，最后更新本地 `syncTimestamp`。

---

## 生产环境切换为阿里云 OSS

1. 实现 `AliyunOssStorageService implements StorageService`
2. `application.yml` 改 `backend: aliyun-oss`，添加 OSS ak/sk/bucket 配置
3. `LocalDiskStorageService` 因 `@ConditionalOnProperty(havingValue = "local")` 自动失效
4. `LocalStorageResourceConfig`（/files/** 静态资源）同样自动失效

业务代码零修改。
