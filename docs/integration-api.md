# 联付中枢对外接入文档

本文档面向需要接入联付中枢的其他项目、客户端、服务端、自动化脚本和 AI 代码生成工具。目标是提供一份完整、稳定、可复用的对外接入说明，而不是只依赖 Swagger 页面临时联调。

## 1. 接入总览

| 项目 | 说明 |
|---|---|
| 服务定位 | 多 APP 支付、会员、设备、云同步、适配上报统一后端 |
| 默认服务地址 | `http://localhost:8888` |
| OpenAPI JSON | `/v3/api-docs` |
| Swagger UI | `/swagger`、`/docs`、`/swagger-ui/index.html` |
| 管理后台 | `/console/` |
| 默认响应格式 | `ApiResponse<T>` |
| 适用接入方 | APP 客户端、业务服务端、支付渠道回调服务、脚本、AI 代码生成工具 |

## 2. 文档使用建议

| 场景 | 建议输入给 AI 的材料 |
|---|---|
| 快速生成 SDK/Client | 本文档 + `/v3/api-docs` |
| 对接启动记录 | 本文档的设备模块 + 启动记录示例 |
| 对接支付 | 本文档的支付模块 + 支付回调说明 |
| 对接会员查询 | 本文档的会员模块 + 鉴权说明 |
| 对接云同步 | 本文档的云同步模块 + 用户 JWT 说明 |

## 3. 统一约定

### 3.1 基础 URL

| 环境 | 示例 |
|---|---|
| 本地 | `http://localhost:8888` |
| 测试 | `https://your-test-host` |
| 生产 | `https://your-prod-host` |

### 3.2 统一响应结构

绝大多数 JSON 接口返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | number | `0` 表示成功；非 `0` 表示业务错误 |
| `message` | string | 结果描述 |
| `data` | any | 业务数据；无数据时可能为 `null` |

### 3.3 HTTP 状态码约定

| 场景 | HTTP 状态 | 说明 |
|---|---|---|
| 业务成功 | `200` | `code=0` |
| 业务异常 | 通常 `200` | 例如资源冲突、未找到、鉴权失败，需看 `code` |
| 参数校验失败 | `400` | 仍可能返回统一错误体 |
| 未捕获异常 | `500` | 返回统一错误体 |

### 3.4 鉴权方式

#### A. 管理员 JWT

用于 `/admin/**` 后台接口。

```http
Authorization: Bearer <admin-token>
```

#### B. 用户 JWT

用于需要用户态的接口，例如 `/api/sync/**`，以及启用用户绑定校验时的标准 APP 会员/订单接口。

```http
Authorization: Bearer <user-token>
```

#### C. APP Secret 模式

当服务端开启 APP 鉴权且模式为 `secret` 时，请求头为：

```http
X-App-Id: demo-app
X-App-Secret: app-secret-plaintext
```

#### D. APP Signature 模式

当服务端开启 APP 鉴权且模式为 `signature` 时，请求头为：

```http
X-App-Id: demo-app
X-App-Timestamp: 1718352000
X-App-Nonce: random-string
X-App-Signature: xxxxx
```

签名原文：

```text
appId + "\n" + timestamp + "\n" + nonce + "\n" + HTTP_METHOD + "\n" + requestPath
```

签名 key：

```text
SHA-256(appSecret 明文) 的十六进制字符串
```

然后用该 hash 作为 HMAC-SHA256 的 key。

### 3.5 APP 类型

| 类型 | 说明 | 典型标识 |
|---|---|---|
| `STANDARD` | 账号会员型 APP | `userId` |
| `DEVICE_ONLY` | 设备会员型 APP | `deviceId` / `deviceCode` |

### 3.6 对接优先级建议

| 接口范围 | 建议 |
|---|---|
| `/api/**` | 对外业务接入主入口 |
| `/api/sync/**` | 用户云同步接入 |
| `/api/adapter/**` | 系统对系统适配上报 |
| `/admin/**` | 不建议外部业务系统直接依赖 |
| `/api/payment/dev/**` | 仅开发联调使用 |

## 4. 对外开放接口总表

### 4.1 推荐外部项目直接接入的接口

| 模块 | 方法 | 路径 | 用途 |
|---|---|---|---|
| 认证 | POST | `/api/auth/send-code` | 发送短信验证码 |
| 认证 | POST | `/api/auth/login` | 用户登录并获取 JWT |
| 设备 | POST | `/api/device/register` | 注册设备 |
| 设备 | POST | `/api/device/launch` | 上报启动记录 |
| 会员 | GET | `/api/member/status` | 查询会员状态 |
| 支付 | POST | `/api/payment/create-order` | 创建支付订单 |
| 支付 | POST | `/api/payment/notify/{payChannel}` | 支付结果回调 |
| 云同步 | POST | `/api/sync/upload` | 上传文件 |
| 云同步 | GET | `/api/sync/list` | 列目录 |
| 云同步 | GET | `/api/sync/{fileId}/url` | 获取下载链接 |
| 云同步 | DELETE | `/api/sync/{fileId}` | 删除文件 |
| 云同步 | GET | `/api/sync/changes` | 增量同步 |
| 适配器 | POST | `/api/adapter/report` | 适配上报 |
| 适配器 | GET | `/api/adapter/status` | 健康检查 |

### 4.2 仅后台或内部使用接口

| 方法 | 路径前缀 | 说明 |
|---|---|---|
| `*` | `/admin/**` | 后台运营、配置、导出、报表、人工处理 |
| POST | `/api/payment/dev/mark-paid` | 开发环境模拟支付 |
| GET | `/console/**` | 后台静态页面 |
| GET | `/swagger`、`/docs` | 文档入口 |

## 5. 模块一：认证接口

### 5.1 发送验证码

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/auth/send-code` |
| 用途 | 发送手机号登录验证码 |
| 推荐接入方 | 客户端、业务服务端 |
| 鉴权 | 通常不需要用户 JWT；若启用 APP 鉴权则需通过 APP Secret/签名 |

请求示例：

```http
POST /api/auth/send-code
Content-Type: application/json

{
  "appId": "demo-app",
  "mobile": "13800000001"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `mobile` | string | 是 | 手机号 |

响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "sent": true,
    "expireSeconds": 300,
    "debugCode": "123456"
  }
}
```

响应字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `sent` | boolean | 是否发送成功 |
| `expireSeconds` | number | 验证码有效期 |
| `debugCode` | string | 开发环境辅助字段，生产不可依赖 |

前置条件：

| 条件 | 说明 |
|---|---|
| APP 已启用 | 否则发送失败 |
| APP 支持手机号登录 | `needMobileLogin=true` |
| 未触发发送冷却 | 默认同手机号有冷却时间 |

### 5.2 用户登录

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/auth/login` |
| 用途 | 验证手机号并签发用户 JWT |
| 鉴权 | 通常不需要用户 JWT；若启用 APP 鉴权则需通过 APP Secret/签名 |

请求示例：

```http
POST /api/auth/login
Content-Type: application/json

{
  "appId": "demo-app",
  "mobile": "13800000001",
  "code": "123456"
}
```

说明：在开发环境、且 `sms-code-required=false` 时，部分场景可只传手机号完成联调。

响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "<user-jwt>",
    "userId": 1,
    "mobile": "13800000001",
    "appId": "demo-app"
  }
}
```

响应字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `token` | string | 用户 JWT |
| `userId` | number | 用户 ID |
| `mobile` | string | 当前手机号 |
| `appId` | string | 当前 APP |

## 6. 模块二：设备接口

### 6.1 注册设备

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/device/register` |
| 用途 | 注册设备或获取已有设备记录 |
| 鉴权 | 通常仅 APP 鉴权；不需要用户 JWT |

请求示例：

```http
POST /api/device/register
Content-Type: application/json

{
  "appId": "demo-app",
  "deviceCode": "device-001",
  "deviceName": "iPhone 15",
  "deviceType": "ios",
  "deviceFingerprint": "fingerprint-001"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `deviceCode` | string | 是 | 设备唯一编码 |
| `deviceName` | string | 否 | 设备名 |
| `deviceType` | string | 否 | 设备类型，如 `ios`、`android` |
| `deviceFingerprint` | string | 否 | 设备指纹 |

响应常见字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | number | 设备 ID |
| `appId` | string | APP 标识 |
| `userId` | number/null | 当前绑定用户 |
| `deviceCode` | string | 设备编码 |
| `bindStatus` | string | 绑定状态 |
| `bindAt` | string/null | 绑定时间 |
| `lastLaunchAt` | string/null | 最近启动时间 |

### 6.2 上报启动记录

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/device/launch` |
| 用途 | 上报设备启动事件 |
| 推荐接入方 | 客户端、宿主 APP、设备代理服务 |
| 鉴权 | 通常仅 APP 鉴权；不需要用户 JWT |

请求示例：

```http
POST /api/device/launch
Content-Type: application/json

{
  "appId": "demo-app",
  "deviceCode": "device-001",
  "userId": 1,
  "platform": "ios",
  "version": "1.0.0",
  "networkType": "wifi",
  "ipAddress": "127.0.0.1",
  "sessionId": "sess-20260614-001",
  "previousSessionId": "sess-20260614-000",
  "previousSessionEndAt": "2026-06-14T10:25:30",
  "previousDurationSeconds": 1530,
  "eventData": "{\"channel\":\"appstore\"}"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `deviceCode` | string | 是 | 设备编码 |
| `userId` | number | 否 | 启动时关联用户 |
| `platform` | string | 否 | 平台 |
| `version` | string | 否 | 客户端版本 |
| `networkType` | string | 否 | 网络类型 |
| `ipAddress` | string | 否 | IP |
| `sessionId` | string | 建议 | 本次启动会话 ID；客户端每次启动生成并持久化到本地 |
| `previousSessionId` | string | 否 | 上次启动会话 ID；下次启动补退出时传入，用于绑定对应 `LAUNCH` |
| `previousSessionEndAt` | string | 否 | 上次退出时间；需和 `previousSessionId` 一起传，匹配到对应启动后回填到该 `LAUNCH` |
| `previousDurationSeconds` | number | 否 | 上次使用时长秒数；不传时后台用对应 `LAUNCH.createdAt` 和 `previousSessionEndAt` 计算 |
| `eventData` | string | 否 | 扩展 JSON 字符串 |

接入建议：

| 建议 | 说明 |
|---|---|
| 首次启动先注册设备 | 没有本地设备标识时先调 `/register` |
| 启动上报不要阻塞主流程 | 失败时建议降级为日志/异步重试 |
| 每次启动生成 `sessionId` | 推荐 UUID 或雪花 ID；启动成功后本地保存，供下次启动补退出使用 |
| 下次启动补传上次退出数据 | 同时传 `previousSessionId` 和 `previousSessionEndAt`；后台匹配不到对应启动时不会回填时长，避免误算超长时长 |
| 保持 `deviceCode` 稳定 | 避免重复建设备 |

会话绑定流程：

| 时机 | 客户端行为 | 后台结果 |
|---|---|---|
| 第 1 次启动 | 生成并上报 `sessionId=A` | 写入 `LAUNCH(A)` |
| 第 1 次退出 | 本地记录退出时间，不立即要求上传 | 不变 |
| 第 2 次启动 | 生成 `sessionId=B`，同时上报 `previousSessionId=A` 和上次退出时间 | 找到 `LAUNCH(A)` 后回填退出时间和时长，再写入 `LAUNCH(B)` |
| 中间启动漏报 | 补传的 `previousSessionId` 找不到 | 不回填时长 |

## 7. 模块三：会员接口

### 7.1 查询会员状态

| 项目 | 内容 |
|---|---|
| 方法 | `GET` |
| 路径 | `/api/member/status` |
| 用途 | 查询账号会员或设备会员状态 |
| 鉴权 | 依 APP 模式而定；标准 APP 且用 `userId` 查询时通常应带用户 JWT |

请求示例（账号会员）：

```http
GET /api/member/status?appId=demo-app&userId=1
Authorization: Bearer <user-token>
```

请求示例（设备会员）：

```http
GET /api/member/status?appId=demo-app&deviceCode=device-001
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `userId` | number | 否 | 用户 ID |
| `deviceId` | number | 否 | 设备 ID |
| `deviceCode` | string | 否 | 设备编码 |

约束：

| 规则 | 说明 |
|---|---|
| 至少提供一个主体 | `userId` 或 `deviceId` 或 `deviceCode` |
| 标准 APP | 更推荐 `userId + user-token` |
| 设备会员 APP | 更推荐 `deviceId/deviceCode` |

响应常见字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `active` | boolean | 是否有效 |
| `status` | string | 会员状态 |
| `expireAt` | string/null | 到期时间 |

## 8. 模块四：支付接口

### 8.1 创建订单

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/payment/create-order` |
| 用途 | 创建支付订单并返回拉起支付所需参数 |
| 鉴权 | 通常需要 APP 鉴权；标准 APP 的用户订单建议带用户 JWT |

请求示例（账号会员）：

```http
POST /api/payment/create-order
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "appId": "demo-app",
  "userId": 1,
  "packageId": 1,
  "payChannel": "ALIPAY"
}
```

请求示例（设备会员）：

```http
POST /api/payment/create-order
Content-Type: application/json

{
  "appId": "demo-device-app",
  "deviceId": 1,
  "packageId": 1,
  "payChannel": "AGGREGATE"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `packageId` | number | 是 | 套餐 ID |
| `userId` | number | 条件必填 | 标准 APP 常用 |
| `deviceId` | number | 条件必填 | 设备会员 APP 常用 |
| `payChannel` | string | 否 | `ALIPAY`、`WECHAT`、`AGGREGATE`，默认支付宝 |

响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "orderNo": "LFP202606140001",
    "amountCents": 990,
    "paymentParams": {
      "provider": "mock-aggregate",
      "configured": true
    }
  }
}
```

响应字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `orderNo` | string | 系统订单号 |
| `amountCents` | number | 金额，单位分 |
| `paymentParams` | object | 支付拉起参数，结构由渠道决定 |

### 8.2 支付回调

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `${domain}/api/payment/notify/{payChannel}` |
| 用途 | 接收第三方支付回调 |
| 鉴权 | 不走 APP Secret/签名鉴权，应由支付渠道验签保障 |

请求示例：

```http
POST ${domain}/api/payment/notify/ALIPAY
Content-Type: application/json

{
  "payload": "verified=true&orderNo=LFP202606140001&tradeNo=T202606140001&status=SUCCESS"
}
```

路径参数：

| 参数 | 说明 |
|---|---|
| `payChannel` | `ALIPAY`、`WECHAT`、`AGGREGATE` |

兼容字段：

| 语义 | 可识别字段 |
|---|---|
| 订单号 | `orderNo`、`order_no`、`out_trade_no` |
| 渠道交易号 | `tradeNo`、`trade_no`、`transaction_id` |
| 成功状态 | `verified=true`、`success=true`、`paid=true`、`status=SUCCESS`、`trade_status=TRADE_SUCCESS` |

回调建议：

| 建议 | 说明 |
|---|---|
| 保证幂等 | 第三方会重复通知 |
| 不依赖前端回跳 | 最终以服务端回调为准 |
| 生产接入时接官方验签 | 当前骨架仅用于本地联调 |

### 8.3 开发模拟支付

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/payment/dev/mark-paid` |
| 用途 | 开发环境一键把订单标记支付成功 |
| 属性 | 仅开发联调使用，禁止外部正式依赖 |

## 9. 模块五：云同步接口

说明：本模块默认要求用户 JWT，适合登录用户的私有配置、图片、增量同步场景。

### 9.1 上传文件

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/sync/upload` |
| 鉴权 | 必须用户 JWT |
| Content-Type | `multipart/form-data` |

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | file | 是 | 文件内容 |
| `path` | string | 否 | 目标虚拟路径，默认 `/` |

响应字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | number | 文件 ID |
| `virtualPath` | string | 虚拟路径 |
| `fileName` | string | 文件名 |
| `contentType` | string | MIME 类型 |
| `sizeBytes` | number | 文件大小 |
| `checksum` | string | SHA-256 |
| `fileCategory` | string | 文件类别 |
| `version` | number | 版本号 |
| `deleted` | boolean | 是否已删除 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 更新时间 |

### 9.2 列目录

| 项目 | 内容 |
|---|---|
| 方法 | `GET` |
| 路径 | `/api/sync/list` |
| 鉴权 | 必须用户 JWT |

请求示例：

```http
GET /api/sync/list?path=/settings
Authorization: Bearer <user-token>
```

### 9.3 获取下载链接

| 项目 | 内容 |
|---|---|
| 方法 | `GET` |
| 路径 | `/api/sync/{fileId}/url` |
| 鉴权 | 必须用户 JWT |

响应常见字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `url` | string | 限时下载地址 |
| `expiresInSeconds` | number | 过期秒数 |

### 9.4 删除文件

| 项目 | 内容 |
|---|---|
| 方法 | `DELETE` |
| 路径 | `/api/sync/{fileId}` |
| 鉴权 | 必须用户 JWT |
| 说明 | 软删除，便于增量同步感知删除事件 |

### 9.5 查询增量变更

| 项目 | 内容 |
|---|---|
| 方法 | `GET` |
| 路径 | `/api/sync/changes` |
| 鉴权 | 必须用户 JWT |

请求示例：

```http
GET /api/sync/changes?since=1718352000000
Authorization: Bearer <user-token>
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `since` | number | 是 | 毫秒时间戳 |

云同步限制：

| 项目 | 默认值 | 说明 |
|---|---|---|
| 配置文件最大大小 | `512 KB` | `max-config-file-bytes` |
| 图片最大大小 | `5 MB` | `max-image-file-bytes` |
| 默认用户配额 | `50 MB` | `default-quota-bytes` |
| 最大文件数 | `500` | `max-file-count` |
| 最大目录深度 | `8` | `max-path-depth` |
| 上传限频 | `20 次/分钟` | `upload-rate-limit-per-minute` |

## 10. 模块六：适配器接口

### 10.1 上报适配数据

| 项目 | 内容 |
|---|---|
| 方法 | `POST` |
| 路径 | `/api/adapter/report` |
| 用途 | 外部适配层/采集层向平台上报业务数据 |
| 鉴权 | 若启用 APP 鉴权，通常需通过 APP Secret/签名 |

请求字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `appId` | string | 是 | APP 标识 |
| `sourceId` | string | 是 | 来源标识 |
| `reportType` | string | 是 | 报告类型 |
| `payload` | string/object | 是 | 上报内容 |

### 10.2 适配器状态检查

| 项目 | 内容 |
|---|---|
| 方法 | `GET` |
| 路径 | `/api/adapter/status` |
| 用途 | 适配器或宿主系统探活 |
| 鉴权 | 若启用 APP 鉴权，通常需通过 APP Secret/签名 |

## 11. 后台接口概览

说明：以下接口存在完整实现，但主要面向运营后台，不建议其他业务系统直接耦合。

| 分组 | 路径前缀 | 能力 |
|---|---|---|
| 管理员认证 | `/admin/auth` | 登录、当前管理员信息 |
| APP 管理 | `/admin/apps` | 创建、查询、启停、重置 secret |
| 套餐管理 | `/admin/packages` | 创建、查询、修改、启停 |
| 支付配置 | `/admin/payment-configs` | 渠道配置维护 |
| 通知配置 | `/admin/notification-configs` | 短信/邮件通道维护与测试 |
| 用户与绑定 | `/admin/users`、`/admin/user-bindings` | 用户和绑定关系维护 |
| 设备/会员/订单 | `/admin/devices`、`/admin/members`、`/admin/orders` | 数据查看与人工处理 |
| 退款 | `/admin/refunds` | 退款申请、人工确认 |
| 报表 | `/admin/reports/*` | 总览、趋势、支付汇总、多维分析 |
| 日志 | `/admin/logs/*` | 操作日志、登录日志、启动日志、支付日志 |
| 导出 | `/admin/exports/*` | CSV 导出 |
| 演示 | `/admin/demo/*` | 构造演示数据 |

## 12. 典型接入流程

### 12.1 标准 APP：手机号登录 + 会员支付

| 步骤 | 接口 | 说明 |
|---|---|---|
| 1 | `/api/auth/send-code` | 发送验证码 |
| 2 | `/api/auth/login` | 获取 `user-token` 和 `userId` |
| 3 | `/api/member/status` | 查询当前会员状态 |
| 4 | `/api/payment/create-order` | 创建订单 |
| 5 | 客户端拉起支付 | 使用 `paymentParams` |
| 6 | `/api/payment/notify/{payChannel}` | 渠道服务端回调确认支付 |
| 7 | `/api/member/status` | 再查会员状态确认生效 |

### 12.2 设备会员 APP：设备启动 + 设备订单

| 步骤 | 接口 | 说明 |
|---|---|---|
| 1 | `/api/device/register` | 首次注册设备 |
| 2 | `/api/device/launch` | 每次启动上报 |
| 3 | `/api/member/status` | 按 `deviceCode` 或 `deviceId` 查会员 |
| 4 | `/api/payment/create-order` | 用 `deviceId` 创建订单 |
| 5 | `/api/payment/notify/{payChannel}` | 渠道回调 |

### 12.3 云同步

| 步骤 | 接口 | 说明 |
|---|---|---|
| 1 | `/api/auth/login` | 获取用户 JWT |
| 2 | `/api/sync/upload` | 上传文件 |
| 3 | `/api/sync/list` | 拉目录 |
| 4 | `/api/sync/changes` | 增量同步 |
| 5 | `/api/sync/{fileId}/url` | 下载文件 |
| 6 | `/api/sync/{fileId}` | 删除文件 |

## 13. curl 示例

### 13.1 启动记录接入

```bash
curl -X POST http://localhost:8888/api/device/register \
  -H 'Content-Type: application/json' \
  -d '{
    "appId":"demo-app",
    "deviceCode":"device-001",
    "deviceName":"iPhone",
    "deviceType":"ios"
  }'
```

```bash
curl -X POST http://localhost:8888/api/device/launch \
  -H 'Content-Type: application/json' \
  -d '{
    "appId":"demo-app",
    "deviceCode":"device-001",
    "platform":"ios",
    "version":"1.0.0"
  }'
```

### 13.2 会员查询

```bash
curl 'http://localhost:8888/api/member/status?appId=demo-app&deviceCode=device-001'
```

### 13.3 创建支付订单

```bash
curl -X POST http://localhost:8888/api/payment/create-order \
  -H 'Content-Type: application/json' \
  -d '{
    "appId":"demo-device-app",
    "deviceId":1,
    "packageId":1,
    "payChannel":"ALIPAY"
  }'
```

### 13.4 获取 OpenAPI

```bash
curl http://localhost:8888/v3/api-docs -o lianpayhub-openapi.json
```

## 14. 给 AI 的推荐输入模板

```text
请基于联付中枢接入文档和 OpenAPI 为我的项目生成接入代码。

基础信息：
- 服务地址：http://localhost:8888
- OpenAPI：http://localhost:8888/v3/api-docs
- 接入文档：docs/integration-api.md
- 接入场景：启动记录 / 支付 / 会员查询 / 云同步 / 适配器上报
- APP 类型：STANDARD 或 DEVICE_ONLY
- 鉴权模式：关闭 / secret / signature

要求：
1. 生成可直接运行的 client/service
2. 封装 DTO、鉴权、错误处理、超时与重试
3. 给出调用示例
4. 不要猜业务流程，严格按文档实现
```

## 15. 当前能力边界与未来预判

### 15.1 当前已适合稳定对接的能力

| 能力 | 状态 |
|---|---|
| 手机号验证码登录 | 可接入 |
| 设备注册与启动记录 | 可接入 |
| 会员状态查询 | 可接入 |
| 订单创建与支付回调 | 可接入 |
| 云同步基础能力 | 可接入 |
| 适配器数据上报 | 可接入 |

### 15.2 未来高概率会新增的能力

| 方向 | 说明 |
|---|---|
| 公开套餐查询 | 外部 APP 通常需要先拉套餐再下单 |
| 订单查询/轮询 | 用于支付结果确认和补偿 |
| 退款公开接口 | 面向外部业务闭环 |
| 刷新 token / 登出 | 完整登录态管理 |
| 更多登录方式 | 第三方登录、设备登录、账号密码 |
| 云同步批量/冲突处理 | 多端同步会进一步需要 |
| 支付渠道官方 SDK 深度对接 | 生产必需 |
| 更细粒度权限 | 多角色、多系统接入常见需求 |

### 15.3 外部项目对接时不要默认依赖的能力

| 能力 | 原因 |
|---|---|
| `/admin/**` 接口 | 面向运营后台，不稳定、权限高 |
| `/api/payment/dev/mark-paid` | 仅开发调试 |
| `debugCode` | 仅开发环境可能返回 |
| 未文档化的内部字段 | 后续可能调整 |

## 16. 文档入口

| 类型 | 地址 |
|---|---|
| OpenAPI JSON | `/v3/api-docs` |
| Swagger UI | `/swagger` |
| Swagger UI 备用 | `/docs` |
| Swagger 默认入口 | `/swagger-ui/index.html` |

## 17. 相关文档

| 文档 | 说明 |
|---|---|
| `README.md` | 项目总览与运行方式 |
| `docs/enterprise-payment-member-system.md` | 业务设计文档 |
| `docs/cloud-sync-file-system.md` | 云同步设计说明 |
| `docs/sql/mysql-5.7-init.sql` | 生产初始化 SQL |
