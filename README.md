# 联付中枢

联付中枢是一个企业级支付与会员管理后端框架，基于 Java + Spring Boot + MySQL。

## 目标

本项目为多个本地 APP 提供统一的：

- APP 管理与配置
- 套餐与订单管理
- 会员状态维护与查询
- 设备码 VIP 判定
- 支付发起与回调处理
- 数据展示与报表分析
- 适配层对接非统一登录/支付 APP

## 技术栈

- Java 8+
- Spring Boot 2.7.x
- Spring Data JPA
- MySQL 5.7+
- Redis（可选；当前验证码、nonce、限频使用本地内存，生产集群建议替换为 Redis）
- Maven

## 目录结构

- `docs/`：设计文档
  - `docs/backend-platform-app-design.md`：后台统一平台配置与多 APP 覆盖设计
  - `docs/admin-roadmap.md`：后台功能优化路线图与优先队列
- `src/main/java/`：后端服务代码
- `src/main/resources/`：配置文件

## 当前已实现

- APP 管理基础接口
- 支付渠道配置管理：按 APP 维护支付宝、微信、聚合支付商户参数和敏感凭据
- 通知通道配置管理：维护阿里云/腾讯云/HTTP 聚合短信、SMTP/云邮件等短信与邮件通道，支持后台测试短信和主动发送邮件
- 套餐管理基础接口
- 设备注册与启动记录
- 手机号验证码发送、登录校验、账号密码登录与用户 APP 绑定
- 用户资料维护：用户名、昵称、头像、手机号、密码、登录记录、设备记录
- 后台用户画像、重置密码、启停、绑定查询、创建与维护
- 设备会员/账号会员状态查询
- 支付订单创建与支付适配层骨架
- 支付统一回调入口、幂等处理、回调日志
- 待支付订单过期时间、后台手动关闭、定时自动关闭
- 后台订单手动标记支付、退款申请、手动确认成功/失败、退款事件日志
- 会员开通/续期基础流程
- 适配型 APP 数据上报
- 统一 API 返回与异常处理
- 后台管理员登录、JWT 鉴权、默认管理员初始化、强制默认密码修改、管理员创建/启停/重置密码/修改自身密码
- 静态管理后台页面：总览、APP、平台（支付/短信/邮件/AI/搜索/存储）、交易（套餐/订单/退款）、用户、绑定、设备、会员、回调、启动、适配、日志、管理员、工具
- 后台用户、用户-APP 绑定、设备绑定/解绑、会员、订单、启动记录分页查询与基础维护
- 后台 APP、支付配置、通知配置、套餐、用户、绑定、设备、会员、订单、退款、回调、启动、适配上报、日志、管理员 CSV 导出
- APP 接入包 Markdown 下载、公开启用套餐列表接口
- 后台总览报表基础指标
- 后台支付汇总报表：按 APP 和支付渠道统计订单、支付订单与收入
- 后台多维统计报表：支持按日/月/年、全部或指定 APP、订单数、启动数、登录数、支付/退款金额等指标生成趋势图
- 后台操作日志、APP 登录日志、启动日志、支付事件日志查询

## 默认管理员

首次启动时，如果 `admin_user` 表里不存在默认用户名，会自动创建：

- 用户名：`admin`
- 密码：`admin123456`

可在 `src/main/resources/application.yml` 中修改：

```yaml
lianpayhub:
  admin:
    default-username: admin
    default-password: admin123456
    force-default-password-change: false
    password-complexity-required: false
    export-enabled: true
  security:
    jwt-secret: change-me-to-a-long-random-secret-for-production
    jwt-expire-minutes: 720
    admin-ip-whitelist: [] # 为空不限制；可填 127.0.0.1、192.168.1.*
```

登录接口：

```http
POST /admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123456"
}
```

后台接口需要携带：

```http
Authorization: Bearer <token>
```

## 接口文档

启动服务后访问：

```text
http://localhost:8888/swagger-ui/index.html
```

后台接口调试步骤：

1. 先调用 `/admin/auth/login` 获取 `token`
2. 点击 Swagger 页面右上角 `Authorize`
3. 输入 `Bearer <token>`
4. 再调用 `/admin/**` 接口

可用 `/admin/auth/me` 验证当前 token 是否有效。

## 管理后台页面

启动服务后访问：

```text
http://localhost:8888/console/
```

默认登录：

```text
admin / admin123456
```

当前页面已包含总览、统计图、APP、平台配置、交易管理、用户、绑定、设备、会员、回调、启动、适配、日志、管理员和调试工具。平台配置内含支付、短信、邮件、AI、APP覆盖、搜索平台 tab；交易管理内含套餐、订单、退款三个 tab，避免侧边栏过长。页面直接调用现有后端 API，适合先看效果和做本地运营联调。控制台采用轻量 Material 操作台布局，带深色/浅色模式、主题色切换和加载反馈，主题偏好会保存在浏览器本地。

常用后台查询接口：

- `GET /admin/apps/{id}/integration-package`
- `GET /admin/users`
- `GET /admin/users/{id}/profile`
- `GET /admin/payment-configs`
- `GET /admin/payment-configs/{id}`
- `GET /admin/payment-configs/{id}/check`
- `GET /admin/notification-configs`
- `GET /admin/notification-configs/{id}`
- `GET /admin/app-platform-policies?appId=xxx`
- `GET /admin/search-platforms`
- `GET /admin/platform-search?appId=xxx&keyword=xxx`
- `GET /admin/user-bindings`
- `GET /admin/user-bindings/{id}`
- `GET /admin/devices`
- `GET /admin/devices/{id}/aggregate`
- `GET /admin/devices/{id}/device-code-logs`
- `GET /admin/members`
- `GET /admin/orders?keyword=<订单号/设备码/交易号/手机号>`
- `GET /admin/orders/{id}`
- `GET /admin/payment-callbacks`
- `GET /admin/payment-refunds`
- `GET /admin/launch-records`
- `GET /admin/launch-records/{id}`
- `GET /admin/adapter-reports`
- `GET /admin/adapter-reports/{id}`
- `GET /admin/reports/overview`
- `GET /admin/reports/trend?days=14`
- `GET /admin/reports/payment-summary`
- `GET /admin/reports/analytics?granularity=DAY&metric=PAID_AMOUNT&periods=30`
- `GET /admin/admin-users`
- `GET /admin/admin-users/{id}`
- `GET /admin/storage`

常用日志查询接口：

- `GET /admin/logs/admin-operations`
- `GET /admin/logs/app-logins`
- `GET /admin/logs/launches`
- `GET /admin/logs/payment-events`
- `GET /admin/exports/{resource}`
- `GET /admin/exports/logs/{logType}`

常用后台维护接口：

- `PUT /admin/apps/{id}`
- `PATCH /admin/apps/{id}/status`
- `POST /admin/apps/{id}/reset-secret`
- `DELETE /admin/apps/{id}`
- `POST /admin/payment-configs`
- `PUT /admin/payment-configs/{id}`
- `DELETE /admin/payment-configs/{id}`
- `PATCH /admin/payment-configs/{id}/status`
- `POST /admin/notification-configs`
- `PUT /admin/notification-configs/{id}`
- `DELETE /admin/notification-configs/{id}`
- `PATCH /admin/notification-configs/{id}/status`
- `POST /admin/notification-configs/sms/send`
- `POST /admin/notification-configs/email/send`
- `PUT /admin/packages/{id}`
- `PATCH /admin/packages/{id}/status`
- `POST /admin/orders/{id}/mark-paid`
- `POST /admin/refunds`
- `POST /admin/refunds/{id}/mark-success`
- `POST /admin/refunds/{id}/mark-failed`
- `POST /admin/demo/device-vip`
- `PATCH /admin/users/{id}/status`
- `PUT /admin/users/{id}/profile`
- `POST /admin/users/{id}/reset-password`
- `POST /admin/user-bindings`
- `PATCH /admin/user-bindings/{id}/status`
- `POST /admin/devices/{id}/bind-user`
- `POST /admin/devices/{id}/unbind`
- `POST /admin/adapter-reports/{id}/mark-processed`
- `POST /admin/adapter-reports/{id}/mark-failed`
- `POST /admin/members/grant`
- `POST /admin/members/{id}/cancel`
- `POST /admin/admin-users`
- `PUT /admin/admin-users/{id}`
- `PATCH /admin/admin-users/{id}/status`
- `POST /admin/admin-users/{id}/reset-password`
- `POST /admin/admin-users/me/change-password`
- `DELETE /admin/search-platforms/{id}`
- `DELETE /admin/ai-platforms/{id}`

本地调试时可以调用 `POST /admin/demo/device-vip` 一键创建演示 APP、套餐、设备、订单，并自动标记支付成功。调试接口 `/api/payment/dev/mark-paid` 默认只用于开发环境，生产配置中已关闭；管理后台订单页使用受管理员 JWT 保护的 `/admin/orders/{id}/mark-paid`。

## 账号体系

当前用户能力已经补齐：

- 用户名、昵称、头像
- 手机号登录和账号密码登录并存
- Access Token + Refresh Token 自动续期，APP 可分别配置两者有效期
- 支持刷新 token、当前会话退出、账号退出所有设备、单设备撤销 refresh token
- 密码设置、修改、重置、默认密码强制改密
- 头像上传、自动压缩、云同步
- 登录失败锁定、token 版本失效、设备黑名单
- 用户画像页：登录记录、设备记录、订单、会员、文件、绑定、AI Key
- 用户级配置同步：支持小体积 `ini/json/xml/yaml` 配置文件，带版本号和删除

常用接口：

- `POST /api/auth/password-login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `POST /api/auth/logout-device`
- `GET /api/user/profile`
- `PUT /api/user/profile`
- `POST /api/user/password/set`
- `POST /api/user/password/change`
- `POST /api/user/mobile/change`
- `POST /api/user/avatar`
- `GET /api/user/login-logs`
- `GET /api/configs`
- `GET /api/configs/changes`
- `GET /api/configs/{key}`
- `PUT /api/configs/{key}`
- `DELETE /api/configs/{key}`

APP 侧常用接口：

- `POST /api/auth/send-code`
- `POST /api/auth/login`
- `POST /api/auth/password-login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `POST /api/auth/logout-device`
- `GET /api/user/profile`
- `PUT /api/user/profile`
- `POST /api/user/password/set`
- `POST /api/user/password/change`
- `POST /api/user/mobile/change`
- `POST /api/user/avatar`
- `GET /api/user/login-logs`
- `GET /api/configs`
- `GET /api/configs/changes`
- `PUT /api/configs/{key}`
- `DELETE /api/configs/{key}`
- `POST /api/device/register`
- `POST /api/device/launch`：启动上报；客户端每次启动生成 `sessionId`；下次启动时补传 `previousSessionId`、`previousSessionEndAt`、`previousDurationSeconds`。后台只在找到同 APP、同设备、同会话 ID 的上一条 `LAUNCH` 时回填退出时间和时长，不再单独写 `EXIT` 行，避免异常漏报导致超长误算
- `GET /api/packages?appId=demo-app`：公开启用套餐列表
- `GET /api/member/status?appId=demo-app&userId=1`
- `GET /api/member/status?appId=demo-app&deviceCode=device-001`
- `POST /api/payment/create-order`
- `POST ${domain}/api/payment/notify/{payChannel}`

## 支付回调

统一回调入口：

```http
POST ${domain}/api/payment/notify/ALIPAY
Content-Type: application/json

{
  "payload": "verified=true&orderNo=LFPxxx&tradeNo=T202605220001&status=SUCCESS"
}
```

`payChannel` 可用：`ALIPAY`（支付宝）、`WECHAT`（微信支付）、`AGGREGATE`（聚合支付）。创建订单不传 `payChannel` 时默认使用支付宝。

支付宝已支持真实下单、异步通知验签、查单和原路退款。创建订单可传 `payMode`：

| payMode | 用途 | 返回字段 |
|---|---|---|
| `QR` | 扫码支付，适合 QT/桌面客户端展示二维码或打开链接 | `qrCode` / `payUrl` |
| `PAGE` | 电脑网站支付，适合 QT 客户端拉起系统浏览器 | `browserUrl` / `payUrl` |
| `APP` | App SDK 支付 | `orderString` |

支付宝配置示例：

```json
// configJson
{"sandbox":true,"gatewayUrl":"https://openapi-sandbox.dl.alipaydev.com/gateway.do","signType":"RSA2","returnUrl":"${domain}/pay.html"}

// credentialJson
{"merchantPrivateKey":"应用私钥","alipayPublicKey":"支付宝公钥"}
```

支付配置是可选的：未配置时仍使用骨架 Provider 方便本地联调；如果某个 APP 的某个渠道配置存在但被停用，创建订单会返回业务冲突。支付宝真实回调支持 `application/x-www-form-urlencoded`，回调地址默认使用 `${domain}/api/payment/notify/ALIPAY`，同步跳转默认使用 `${domain}/pay.html`；下单时会按当前请求域名与端口替换 `${domain}`。

管理后台 `工具 -> 模拟支付回调` 可用来本地验证订单支付成功和会员生效链路。

退款手动确认只能处理 `PENDING` 退款单，重复确认会返回业务冲突，避免订单重复累计退款金额。

订单默认 30 分钟过期，可通过 `lianpayhub.payment.order-expire-minutes` 调整。后台订单页可以手动关闭待支付订单；定时任务会按 `lianpayhub.payment.order-close-scan-fixed-delay-ms` 扫描并自动关闭过期待支付订单。关闭后的订单不会再接受支付回调、手动标记支付或退款申请，相关状态变更会写入支付事件日志。

## 手机号验证码

`POST /api/auth/send-code` 会校验 APP 是否启用且支持手机号登录，然后生成 6 位验证码。开发环境默认在响应中返回 `debugCode` 方便联调；生产配置应关闭：

```yaml
lianpayhub:
  security:
    sms-code-required: true
    sms-debug-return-code: false
```

## 数据库迁移

项目已接入 Flyway。默认本地开发仍使用 `spring.jpa.hibernate.ddl-auto=update` 并关闭 Flyway，生产配置 `application-prod.yml` 启用 Flyway 且使用 `ddl-auto=validate`。

- 初始化脚本：`docs/sql/mysql-5.7-init.sql`
- Flyway 迁移：`src/main/resources/db/migration/V1__init_schema.sql`、`src/main/resources/db/migration/V2__app_platform_policy.sql`
- 生产升级建议：后续表结构变化新增递增版本迁移，例如 `V3__*.sql`，不要直接修改已发布迁移。

相关配置：

- `sms-code-expire-minutes`：验证码有效期，默认 5 分钟
- `sms-code-cooldown-seconds`：同一 APP + 手机号发送冷却，默认 60 秒
- `sms-code-max-attempts`：错误尝试上限，默认 5 次，超过后需重新获取

当前验证码存储和发送限频使用本地内存，单机部署可直接使用；多实例生产部署建议把验证码、nonce 和限频窗口迁移到 Redis，并接入真实短信服务商发送短信。验证码在登录成功后会被消费，不能重复使用。

短信发送已抽象为通知通道，默认 `sms-provider: aliyun`。后台 `平台配置 -> 短信配置` 可以维护阿里云、腾讯云、HTTP 聚合短信或本地日志通道，并支持测试发送。阿里云短信和腾讯云短信已接入官方 Java SDK，填好密钥、签名、模板 ID 后即可真实提交发送；`local` 仅用于本地日志调试。

APP 级覆盖策略可在 `平台配置 -> APP覆盖` 中配置：

- `SMS`：`providerCode` 覆盖短信供应商，`policyJson.cooldownSeconds/expireMinutes` 覆盖发送冷却和有效期。
- `CAPTCHA`：`policyJson.ttlSeconds/length/maxAttempts/debugReturnCode` 控制独立验证码挑战。
- `PAYMENT`：`policyJson.defaultPayChannel` 控制未传支付渠道时的 APP 默认支付渠道。
- `AI`：`providerCode` 可作为 APP 默认 AI 供应商，`enabled=false` 会禁用该 APP 的 AI 网关和自动 Key 发放。
常用短信配置字段：

搜索平台如博查等第三方检索服务，统一放在 `平台配置 -> 搜索平台`。

- 阿里云 `aliyun`：`credentialJson` 填 `accessKeyId/accessKeySecret`，`configJson` 填 `templateCode`，签名可填 `senderName` 或 `configJson.signName`。
- 腾讯云 `tencent`：`credentialJson` 填 `secretId/secretKey`，`configJson` 填 `sdkAppId/templateId/region/templateParamKeys`，签名可填 `senderName` 或 `configJson.signName`。
- HTTP 聚合 `aggregate`：`endpoint` 填平台接口地址，`credentialJson` 可填 `apiKey` 或 `token`，`configJson.headers/extraBody` 可补充平台需要的固定头和固定参数。

## 邮件发送

后台 `平台配置 -> 邮件配置` 可以维护 SMTP、阿里云邮件推送、腾讯云 SES 等邮件通道。当前 SMTP 已支持真实发送：普通配置示例为 `{"host":"smtp.example.com","port":465,"ssl":true,"smtpAuth":true}`，敏感凭据示例为 `{"username":"noreply@example.com","password":"授权码"}`；非 SMTP 云邮件 provider 暂时走日志型占位发送，后续接官方 SDK 时复用同一套配置、脱敏和后台操作日志。

## APP 接口鉴权

开发阶段默认关闭 APP 接口鉴权：

```yaml
lianpayhub:
  security:
    api-auth-enabled: false
```

生产环境建议开启：

```yaml
lianpayhub:
  security:
    api-auth-enabled: true
    api-auth-mode: secret
```

`secret` 模式下，调用 `/api/**` 需要携带：

```http
X-App-Id: demo-app
X-App-Secret: 创建或重置 APP 时返回的明文 secret
```

服务端只保存 `app_secret_hash`，明文 secret 只在创建或重置时返回一次。

也可以切换到签名模式：

```yaml
lianpayhub:
  security:
    api-auth-enabled: true
    api-auth-mode: signature
    api-signature-time-window-seconds: 300
```

签名模式请求头：

```http
X-App-Id: demo-app
X-App-Timestamp: 当前 Unix 秒级时间戳
X-App-Nonce: 每次请求唯一随机串
X-App-Signature: 签名结果
```

签名原文：

```text
appId + "\n" + timestamp + "\n" + nonce + "\n" + HTTP_METHOD + "\n" + requestPath
```

签名 key 使用 `SHA-256(appSecret 明文)` 的十六进制结果。服务端保存的也是该 hash，不保存明文 secret；客户端拿到创建或重置 APP 时返回的明文 secret 后，先计算同样的 SHA-256 hash，再用该 hash 做 HMAC-SHA256。

支付回调 `/api/payment/notify/{payChannel}` 不走 APP Secret/签名鉴权，后续接真实支付宝、微信或聚合支付时应在对应 `PaymentProvider` 中做官方验签。设备码会员 APP（`DEVICE_ONLY`）的设备注册、启动、会员状态查询和创建订单不强制 APP Secret/签名，但仍有接口限频；标准 APP 仍需要按上述方式鉴权。

开启 `api-auth-enabled` 后，标准 APP 的账号会员查询和账号订单创建还需要携带 `/api/auth/login` 返回的用户 JWT：

```http
Authorization: Bearer <user-token>
```

服务端会校验用户 token 中的 `appId`、`userId` 与请求参数一致，且用户和绑定关系处于启用状态。设备码会员 APP 仍按 `deviceId/deviceCode` 识别会员，不要求用户 JWT。

`/api/auth/refresh` 和 `/api/auth/logout` 使用 refresh token 自身作为凭证，即使开启 APP 接口鉴权，也不需要额外携带 APP Secret。

## 运行方式

1. 先准备 MySQL 环境；生产集群建议额外准备 Redis
2. 修改 `src/main/resources/application.yml` 中的数据库配置
3. 执行：

```bash
mvn org.springframework.boot:spring-boot-maven-plugin:2.7.18:run
```

如果你的 Maven 能正常解析 Spring Boot 插件前缀，也可以使用：

```bash
mvn spring-boot:run
```

## 数据库初始化

开发环境可以继续使用：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

生产或服务器部署建议先执行初始化 SQL：

```text
docs/sql/mysql-5.7-init.sql
```

然后使用 `prod` 配置启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

`application-prod.yml` 中使用：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

这样服务只校验表结构，不会在生产环境自动改表。

## 开发热更新

项目已接入 Spring Boot DevTools。开发时用下面命令启动：

```bash
mvn spring-boot:run
```

修改 Java 代码后，需要 IDE 或 Maven 触发重新编译，DevTools 会自动重启应用。VS Code 中建议安装 Java 扩展并开启自动编译；如果没有自动编译，可以手动执行：

```bash
mvn compile
```

配置文件和静态资源变更通常会触发重启；生产环境不要依赖 DevTools。

## 验证

轻量编译：

```bash
mvn -q -DskipTests compile
```

后台与支付主链路烟测：

```bash
mvn -q -Dtest=AdminApiSmokeTest test
```

该烟测会使用 H2 内存库验证：默认管理员初始化、后台登录、管理员维护、创建 APP、手机号登录与绑定、支付配置脱敏与启停、创建套餐、设备注册、创建订单、支付回调、会员状态生效、退款重复确认保护。

完整流程回归测试：

```bash
mvn -q -Dtest=FullWorkflowIntegrationTest test
```

该测试同样使用 H2 内存库，不连接 `application.yml` 中配置的 MySQL，因此不会影响本地或服务器现有数据，也不会推进现有表的自增索引。覆盖顺序包括：后台页面与鉴权、管理员维护、APP/支付配置/通知配置/套餐维护、测试短信与主动邮件发送、手机号跨 APP 统一用户、用户与绑定启停、设备注册/绑定/启动、会员赠送/取消、账号订单手动支付、设备会员订单回调与重复回调、退款成功/失败、适配上报、日志查询、多维报表查询和演示数据接口。

如果机器内存较紧，可以先设置 Maven 内存参数再运行：

```powershell
$env:MAVEN_OPTS='-Xms16m -Xmx96m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=16m'
mvn -q -Dtest=FullWorkflowIntegrationTest test
```

Windows 本地也可以直接运行完整回归脚本：

```powershell
.\scripts\test-full.ps1
```

脚本会先编译，再按顺序运行 APP Secret 鉴权、签名鉴权、用户 JWT、短信验证码、订单生命周期、后台导出、完整业务流程和主链路烟测，并使用 H2 内存库，不会连接 MySQL。若已经编译过，可以加 `-SkipCompile`：

```powershell
.\scripts\test-full.ps1 -SkipCompile
```

## 后续计划

- 接入真实支付宝、微信或聚合支付 SDK 与验签配置
- 补充更多短信平台适配、云邮件 SDK 和真实支付 SDK
- 补充更细的后台角色/权限与 APP 级数据范围
- 增加更多报表和定时汇总任务

## 文档

- 对外接入文档：`docs/integration-api.md`
- 设计文档：`docs/enterprise-payment-member-system.md`
- 云同步设计文档：`docs/cloud-sync-file-system.md`
- 初始化 SQL：`docs/sql/mysql-5.7-init.sql`
