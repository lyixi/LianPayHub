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
- Redis（后续缓存/验证码可接入，当前核心链路不强依赖）
- Maven

## 目录结构

- `docs/`：设计文档
- `src/main/java/`：后端服务代码
- `src/main/resources/`：配置文件

## 当前已实现

- APP 管理基础接口
- 支付渠道配置管理：按 APP 维护支付宝、微信、聚合支付商户参数和敏感凭据
- 套餐管理基础接口
- 设备注册与启动记录
- 手机号账号登录与用户 APP 绑定
- 后台用户-APP 绑定查询、创建、启停
- 设备会员/账号会员状态查询
- 支付订单创建与支付适配层骨架
- 支付统一回调入口、幂等处理、回调日志
- 后台订单手动标记支付、退款申请、手动确认成功/失败、退款事件日志
- 会员开通/续期基础流程
- 适配型 APP 数据上报
- 统一 API 返回与异常处理
- 后台管理员登录、JWT 鉴权、默认管理员初始化、管理员创建/启停/重置密码/修改自身密码
- 静态管理后台页面：总览、APP、支付配置、套餐、用户、绑定、设备、会员、订单、退款、回调、启动、适配、日志、管理员、工具
- 后台用户、用户-APP 绑定、设备绑定/解绑、会员、订单、启动记录分页查询与基础维护
- 后台总览报表基础指标
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
  security:
    jwt-secret: change-me-to-a-long-random-secret-for-production
    jwt-expire-minutes: 720
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
http://localhost:8080/swagger-ui/index.html
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
http://localhost:8080/console/
```

默认登录：

```text
admin / admin123456
```

当前页面已包含总览、APP、支付配置、套餐、用户、绑定、设备、会员、订单、退款、回调、启动、适配、日志、管理员和调试工具。页面直接调用现有后端 API，适合先看效果和做本地运营联调。

常用后台查询接口：

- `GET /admin/users`
- `GET /admin/payment-configs`
- `GET /admin/payment-configs/{id}`
- `GET /admin/user-bindings`
- `GET /admin/user-bindings/{id}`
- `GET /admin/devices`
- `GET /admin/members`
- `GET /admin/orders`
- `GET /admin/orders/{id}`
- `GET /admin/payment-callbacks`
- `GET /admin/payment-refunds`
- `GET /admin/launch-records`
- `GET /admin/launch-records/{id}`
- `GET /admin/adapter-reports`
- `GET /admin/adapter-reports/{id}`
- `GET /admin/reports/overview`
- `GET /admin/reports/trend?days=14`
- `GET /admin/admin-users`
- `GET /admin/admin-users/{id}`

常用日志查询接口：

- `GET /admin/logs/admin-operations`
- `GET /admin/logs/app-logins`
- `GET /admin/logs/launches`
- `GET /admin/logs/payment-events`

常用后台维护接口：

- `PUT /admin/apps/{id}`
- `PATCH /admin/apps/{id}/status`
- `POST /admin/apps/{id}/reset-secret`
- `POST /admin/payment-configs`
- `PUT /admin/payment-configs/{id}`
- `PATCH /admin/payment-configs/{id}/status`
- `PUT /admin/packages/{id}`
- `PATCH /admin/packages/{id}/status`
- `POST /admin/orders/{id}/mark-paid`
- `POST /admin/refunds`
- `POST /admin/refunds/{id}/mark-success`
- `POST /admin/refunds/{id}/mark-failed`
- `POST /admin/demo/device-vip`
- `PATCH /admin/users/{id}/status`
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

本地调试时可以调用 `POST /admin/demo/device-vip` 一键创建演示 APP、套餐、设备、订单，并自动标记支付成功。调试接口 `/api/payment/dev/mark-paid` 默认只用于开发环境，生产配置中已关闭；管理后台订单页使用受管理员 JWT 保护的 `/admin/orders/{id}/mark-paid`。

APP 侧常用接口：

- `POST /api/auth/send-code`
- `POST /api/auth/login`
- `POST /api/device/register`
- `POST /api/device/launch`
- `GET /api/member/status?appId=demo-app&userId=1`
- `GET /api/member/status?appId=demo-app&deviceCode=device-001`
- `POST /api/payment/create-order`
- `POST /api/payment/notify/{payChannel}`

## 支付回调

统一回调入口：

```http
POST /api/payment/notify/ALIPAY
Content-Type: application/json

{
  "payload": "verified=true&orderNo=LFPxxx&tradeNo=T202605220001&status=SUCCESS"
}
```

`payChannel` 可用：`ALIPAY`、`WECHAT`、`AGGREGATE`。

当前 Provider 先支持通用 `form` / 简单 JSON 字段解析，字段兼容：

- 订单号：`orderNo`、`order_no`、`out_trade_no`
- 渠道交易号：`tradeNo`、`trade_no`、`transaction_id`
- 成功状态：`verified=true`、`success=true`、`paid=true`、`status=SUCCESS`、`trade_status=TRADE_SUCCESS`

真实接支付宝、微信或聚合支付时，先在后台 `支付配置` 为 APP 维护商户号、渠道 APP ID、回调地址、普通配置和敏感凭据；再替换对应 `PaymentProvider.parseCallback` 内部的官方验签和字段解析逻辑。`PaymentService` 的订单入账、会员开通、回调日志、幂等处理可以复用。

支付配置是可选的：未配置时仍使用骨架 Provider 方便本地联调；如果某个 APP 的某个渠道配置存在但被停用，创建订单会返回业务冲突。

管理后台 `工具 -> 模拟支付回调` 可用来本地验证订单支付成功和会员生效链路。

退款手动确认只能处理 `PENDING` 退款单，重复确认会返回业务冲突，避免订单重复累计退款金额。

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

当前 1.0 实现使用服务端保存的 `app_secret_hash` 作为 HMAC-SHA256 校验 key 的派生材料；后续如需标准客户端 HMAC，可改为加密保存明文 secret 或引入独立 signing key。

## 运行方式

1. 先准备 MySQL 和 Redis 环境
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

该测试同样使用 H2 内存库，不连接 `application.yml` 中配置的 MySQL，因此不会影响本地或服务器现有数据，也不会推进现有表的自增索引。覆盖顺序包括：后台页面与鉴权、管理员维护、APP/支付配置/套餐维护、手机号跨 APP 统一用户、用户与绑定启停、设备注册/绑定/启动、会员赠送/取消、账号订单手动支付、设备会员订单回调与重复回调、退款成功/失败、适配上报、日志查询、报表查询和演示数据接口。

如果机器内存较紧，可以先设置 Maven 内存参数再运行：

```powershell
$env:MAVEN_OPTS='-Xms16m -Xmx96m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=16m'
mvn -q -Dtest=FullWorkflowIntegrationTest test
```

## 后续计划

- 接入真实支付宝、微信或聚合支付 SDK 与验签配置
- 补充更细的后台角色/权限与 APP 级数据范围
- 增加导出、更多报表和定时汇总任务

## 文档

- 设计文档：`docs/enterprise-payment-member-system.md`
