# ACE Data 接入

## 后台使用

在 `/console/` → AI 平台中新建平台，选择 **ACE Data**，填写 API Key 并保存。
该平台的服务地址固定为 `https://api.acedata.cloud`，无需配置链接、Cookie 或管理 Token。
编辑时密钥留空表示保留现有密钥。

平台列表中的「接口工作台」提供文字与多模态、图片、视频、音频音乐、任务查询五类接口。
当前按官方文档收录 81 个 JSON 接口，参数模板、参数说明和文档链接随接口切换。
模板取自文档示例，需要按实际用途填写模型、提示词和资源地址；模板本身不保证符合所有模型的条件参数要求。

- 请求会消耗 ACE Data 账户额度。
- 当前支持非流式 HTTP JSON；不包含 WebSocket、文件 multipart 上传接口和第三方平台的非 AI 类业务。
- 图片编辑支持官方 JSON 形式的资源输入；文件上传形式暂未接入。
- 长任务建议使用 `async: true`，记录返回的 `task_id`，切换到同平台的任务查询接口继续查询。各平台的任务参数不同，以模板和官方文档为准。
- 工作台显示完整响应和结果文件链接，语音等二进制响应可下载。
- 请求最多等待 5 分钟。超时后先查询任务状态，避免重复生成和扣费。
- 当前未接入 ACE Data 余额查询和本地自动计费。

## 应用接口

现有 `POST /api/ai/chat` 可使用 `providerCode: "acedata"`，调用 OpenAI 兼容聊天接口。

其他能力统一使用 `POST /api/ai/acedata/invoke`：

```json
{
  "appId": "your-app",
  "operation": "minimax-videos",
  "parameters": {
    "model": "MiniMax-H3",
    "ratio": "adaptive",
    "content": [{ "type": "text", "text": "海浪缓缓拍打岸边" }],
    "async": true
  }
}
```

需要用户登录、用户对 APP 的访问权限、APP 启用 ACE Data、有效的用户 AI Key，以及处于启用状态的平台配置。
应用调用沿用用户 Key；后台工作台使用平台 Key。不支持自动创建上游用户 Key。

管理员接口：

- `GET /admin/ai-platforms/acedata/operations`：接口目录、请求 schema 和官方文档链接。
- `POST /admin/ai-platforms/{id}/acedata/invoke`：请求体为 `operation` 和 `parameters`，使用该平台已保存的密钥。

接口白名单与文档 schema 位于 `src/main/resources/ai/acedata-operations.json`，来源为
`https://platform.acedata.cloud/api/v1/documents/?limit=1000&offset=0`，收录日期为 2026-10-26。
所有请求均发送至固定官方域名，客户端不能指定转发地址。

## 后台旧数据排查（2026-10-26）

本次只读检查本机 MySQL 的 `lianpayhub` 库，实际 `COUNT(*)` 结果为：

| 表 | 记录数 |
| --- | ---: |
| app_info | 0 |
| user_info | 0 |
| payment_order | 0 |
| ai_provider_config | 0 |

这解释了当前业务后台为空，不能证明数据被重构删除。官网内容使用独立的 `website_*` 表，仍存在数据。
官网重构提交 `e7ca63dd` 没有修改数据库连接地址，当前默认仍连接本机 `lianpayhub`。
检查范围内没有发现 SQL 数据备份；现存的 2026-09-25 起 MySQL binlog 中没有找到上述业务表的写入或删除记录。
因此目前无法确定丢失原因，也未执行恢复、初始化或造数。恢复需要包含原业务记录的数据库备份、旧数据库实例或更早的 binlog。
