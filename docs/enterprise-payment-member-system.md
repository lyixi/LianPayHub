# 企业级支付与会员管理平台设计文档

## 1. 项目定位

目标：构建一个企业级统一支付与会员管理后端框架，支持多个 APP 的统一管理、支付套餐管理、会员状态管理、订单与回调、数据展示与报表，以及可扩展的 APP 适配层。

本项目应提供：
- 后台管理端：APP、套餐、用户、会员、订单、启动记录、报表；
- APP 统一接口：登录认证、设备码 VIP 判定、会员查询、支付发起；
- 适配层支持：对于不使用统一登录/支付服务的 APP，也能接入运行数据与状态监控；
- 支付渠道：支持支付宝、微信、聚合支付，通过统一支付适配层扩展；
- 技术栈：Java 8 + Spring Boot 2.7，MySQL 5.7，Redis 可选，前端可选 Vue/React。

---

## 2. 体系结构

### 2.1 总体架构

- 管理后台：支持运营人员管理 APP、套餐、订单、会员、报表；
- 统一后端服务：处理认证、会员、支付、设备、日志、报表；
- 支付适配层：封装支付宝、微信、聚合支付等不同渠道的下单、验签、回调解析；
- APP 适配层：对接特定 APP 的业务侧数据上报与状态映射；
- 数据库：MySQL 持久化核心业务数据；
- 缓存：开发阶段可用本地内存，生产集群建议接 Redis 存储验证码、nonce、限频和热点查询；
- 异步消息：RabbitMQ/RocketMQ（可选）用于订单回调、报表计算、告警任务。

架构图（建议实现）：
- 前端管理台
- 后端 API 网关 + 业务服务
- 支付适配层
- MySQL
- Redis
- 日志/监控系统

### 2.2 模块划分

1. 管理模块
   - APP 管理
   - 套餐管理
   - 用户、用户-APP 绑定、会员管理
   - 订单与支付管理
   - 管理员账号管理
   - 后台操作日志、APP 登录日志、启动与设备日志、支付事件日志、回调日志、适配上报日志
   - 数据看板与报表

2. 核心业务模块
   - 认证模块
   - 会员模块
   - 支付模块
   - 设备模块
   - 适配层模块
   - 启动记录与适配上报查询模块

3. 通用模块
   - 安全鉴权
   - 日志审计
   - 异常与监控
   - 配置中心

---

## 3. 关键概念与场景

### 3.1 APP 类型

- 标准 APP：需要手机号登录，使用统一账号体系；各 APP 的会员套餐、支付订单、运营数据独立。
- 设备码 VIP APP：无需手机号登录，使用本地设备码判定 VIP；一个设备码视为一个设备用户，会员权益归属到设备。
- 适配型 APP：无需使用该平台的登录或支付，但希望上报运行数据、状态、启动记录，统一在管理后台展示。

### 3.2 当前业务决策

- 后台管理员在 1.0 阶段可以查看和管理所有 APP，暂不做后台 APP 级数据权限隔离。
- 一个手机号跨多个 APP 视为同一个统一用户，`user_info.mobile` 全局唯一。
- 标准 APP 使用 `user_id` 作为会员主体；设备码 VIP APP 使用 `device_id` 作为会员主体。
- 标准 APP 也可以记录设备码，后台可将设备绑定到统一用户；设备记录用于启动日志、风控、设备管理，不直接和账号会员混用。
- 设备码 VIP APP 换机需要重新支付，特殊情况可由管理员人工处理设备会员迁移。
- APP 侧业务接口在逻辑上都应能定位到具体 `app_id`；实现上可以从请求头、Token、签名凭据或请求参数解析，不要求所有接口都显式传 query 参数。
- APP 侧接口生产环境建议开启鉴权；开发阶段可通过配置关闭，方便 Swagger 和本地联调。
- 1.0 支持两种 APP 鉴权模式：`secret` 模式使用 `X-App-Id` + `X-App-Secret`；`signature` 模式使用 `X-App-Id` + 时间戳 + nonce + HMAC 签名。

### 3.3 业务边界

- 认证：手机号登录、Token 管理、APP 请求鉴权；
- 支付：订单创建、渠道发起、统一回调、回调验签、幂等入账、会员开通；
- 会员：会员状态维护、到期管理、续费、权益；
- 设备：deviceCode 注册、绑定、启动上报；
- 报表：用户/会员/付费数据统计、增长趋势、渠道分析。
- 日志：后台操作审计、APP 登录日志、启动日志、支付/退款事件日志、适配上报时间线。

---

## 4. 数据模型

### 4.0 `admin_user`

- id
- username
- password_hash
- display_name
- status (`ENABLED`, `DISABLED`)
- last_login_at
- created_at
- updated_at

- note: 默认管理员由启动初始化器创建；后台支持管理员创建、显示名修改、启停、重置密码和当前账号修改密码。

### 4.1 `app_info`

- id
- app_id
- app_name
- app_secret_hash
- app_secret_version
- app_type (`STANDARD`, `DEVICE_ONLY`, `ADAPTER`)
- need_mobile_login (boolean)
- need_device_vip (boolean)
- status
- created_at
- updated_at

### 4.2 `user_info`

- id
- mobile (unique)
- user_type (`ACCOUNT`, `GUEST`)
- open_id
- status
- created_at
- updated_at

### 4.3 `user_app_binding`

- id
- user_id
- app_id
- bind_type (`MOBILE_LOGIN`, `DEVICE_BIND`)
- bind_at
- status
- created_at
- updated_at

### 4.4 `device_info`

- id
- app_id
- user_id (nullable)
- device_code
- device_name
- device_type
- device_fingerprint
- bind_status
- bind_at
- last_launch_at
- created_at
- updated_at

- note: 设备码在同一 APP 内唯一。标准 APP 可同时记录 `user_id` 与 `device_code`；设备码 VIP APP 可以没有 `user_id`。

### 4.5 `package_info`

- id
- app_id
- package_name
- package_type (`MEMBERSHIP`, `FEATURE`)
- price_cents
- duration_days
- benefits_text
- status
- created_at
- updated_at

### 4.6 `payment_order`

- id
- app_id
- user_id
- device_id
- package_id
- order_no
- amount_cents
- pay_channel (`ALIPAY`, `WECHAT`, `AGGREGATE`, `OTHER`)
- pay_provider
- pay_status (`PENDING`, `PAID`, `FAILED`, `CANCELLED`, `PARTIAL_REFUNDED`, `REFUNDED`)
- trade_no
- channel_order_no
- callback_data
- callback_count
- refunded_amount_cents
- created_at
- paid_at
- updated_at

- note: `order_no` 全局唯一。支付回调按 `order_no` 查询订单，已支付订单的重复回调记录为 `IGNORED`，不会重复开通会员。

### 4.6.1 `payment_channel_config`

- id
- app_id
- pay_channel (`ALIPAY`, `WECHAT`, `AGGREGATE`)
- provider_code
- merchant_id
- channel_app_id
- notify_url
- config_json
- credential_json
- status (`ENABLED`, `DISABLED`)
- created_at
- updated_at

- note: 每个 APP 每个支付渠道最多一份配置。普通配置可以回显给后台，`credential_json` 用于商户私钥、API Key、证书密码等敏感凭据，接口响应不回显，后台操作日志会脱敏。当前配置为可选：未配置时仍使用骨架 Provider 方便本地联调；配置存在但停用时会禁止创建该渠道订单。

### 4.6.2 支付配置与创建订单

创建订单时，`PaymentService` 会按 `app_id + pay_channel` 查找支付配置：

1. 无配置：继续使用当前支付渠道的骨架 Provider，便于开发联调；
2. 配置启用：订单 `pay_provider` 使用配置中的 `provider_code`，支付参数返回商户号、渠道 APP ID、回调地址和普通配置；
3. 配置停用：返回业务冲突，禁止继续创建订单；
4. 敏感凭据仅保存在服务端，后续接入支付宝、微信或聚合支付 SDK 时由 Provider 内部读取使用。

### 4.7 `member_info`

- id
- app_id
- member_subject_type (`USER`, `DEVICE`)
- user_id (nullable)
- device_id (nullable)
- package_id
- status (`ACTIVE`, `EXPIRED`, `CANCELLED`)
- start_at
- expire_at
- order_id
- created_at
- updated_at

- note: 用户可以在不同 APP 下拥有不同会员状态，会员记录与套餐归属到 APP 级别。标准 APP 以 `user_id` 查询会员，设备码 VIP APP 以 `device_id` 查询会员。

### 4.8 `payment_callback_log`

- id
- app_id
- order_id
- pay_channel
- pay_provider
- trade_no
- raw_payload
- verify_status
- process_status
- error_message
- created_at

### 4.9 `payment_refund`

- id
- app_id
- order_id
- refund_no
- amount_cents
- reason
- status (`PENDING`, `SUCCESS`, `FAILED`)
- channel_refund_no
- processed_at
- created_at
- updated_at

- note: 退款单独建模，支持一笔订单多次部分退款；订单通过 `refunded_amount_cents` 和状态反映累计退款结果。后台手动确认只允许处理 `PENDING` 退款单，重复确认会返回业务冲突。

### 4.10 `payment_event_log`

- id
- app_id
- order_id
- event_type (`ORDER_CREATED`, `PAYMENT_SUCCESS`, `PAYMENT_FAILED`, `REFUND_CREATED`, `REFUND_SUCCESS`, `REFUND_FAILED`)
- pay_channel
- pay_provider
- amount_cents
- trade_no
- operator_type (`SYSTEM`, `ADMIN`, `CHANNEL`)
- operator_id
- event_data
- created_at

- note: 支付回调日志记录渠道原始回调；支付事件日志记录业务侧可读事件，用于后台排查付款、退款、补偿、异常订单。

### 4.10.1 支付回调处理

统一回调入口：

- `POST /api/payment/notify/{payChannel}`
- `payChannel`: `ALIPAY`、`WECHAT`、`AGGREGATE`

请求体：

```json
{
  "payload": "verified=true&orderNo=LFPxxx&tradeNo=T202605220001&status=SUCCESS"
}
```

处理流程：

1. Controller 根据 `payChannel` 找到对应 `PaymentProvider`。
2. Provider 负责验签与字段解析，输出 `PaymentCallbackResult`。
3. Service 校验订单号、支付状态与幂等状态。
4. 首次成功回调更新订单为 `PAID`，写入 `payment_callback_log`、`payment_event_log`，并调用会员服务开通或续期。
5. 重复成功回调只写入 `payment_callback_log`，处理状态为 `IGNORED`。
6. 验签失败、缺少订单号、订单不存在等异常会记录失败日志或返回业务错误。

当前实现提供通用 form / 简单 JSON 解析，真实渠道接入时替换支付宝、微信、聚合支付 Provider 内部解析即可。

### 4.11 `admin_operation_log`

- id
- admin_id
- username
- operation_type
- target_type
- target_id
- request_method
- request_uri
- ip_address
- user_agent
- request_body
- result_status (`SUCCESS`, `FAILED`)
- error_message
- created_at

- note: 后台操作日志用于审计管理员对 APP、套餐、订单、会员等资源的变更和查询行为。密码、Token、APP Secret、支付渠道敏感凭据等字段落库前会做脱敏。

### 4.12 `app_login_log`

- id
- app_id
- user_id
- mobile
- login_type (`MOBILE`, `DEVICE`)
- device_id
- device_code
- ip_address
- user_agent
- result_status (`SUCCESS`, `FAILED`)
- error_message
- created_at

- note: APP 登录日志只记录登录认证事件；启动日志独立记录 APP 打开/唤起事件，未登录用户和无需登录 APP 也会产生启动日志。

### 4.13 `launch_record`

- id
- app_id
- device_id
- user_id
- platform
- version
- network_type
- ip_address
- event_type (`LAUNCH`, `LOGIN`, `PAYMENT`)
- event_data
- created_at

### 4.14 `adapter_report`

- id
- app_id
- source_id
- report_type
- status
- payload
- created_at
- updated_at

---

## 5. API 设计

### 5.1 管理后台 API

- `POST /admin/auth/login`
- `GET /admin/auth/me`
- `POST /admin/apps`
- `GET /admin/apps`
- `PUT /admin/apps/{id}`
- `PATCH /admin/apps/{id}/status`
- `POST /admin/apps/{id}/reset-secret`
- `GET /admin/payment-configs`
- `GET /admin/payment-configs/{id}`
- `POST /admin/payment-configs`
- `PUT /admin/payment-configs/{id}`
- `PATCH /admin/payment-configs/{id}/status`
- `POST /admin/packages`
- `GET /admin/packages`
- `PUT /admin/packages/{id}`
- `PATCH /admin/packages/{id}/status`
- `GET /admin/users`
- `PATCH /admin/users/{id}/status`
- `GET /admin/user-bindings`
- `GET /admin/user-bindings/{id}`
- `POST /admin/user-bindings`
- `PATCH /admin/user-bindings/{id}/status`
- `GET /admin/devices`
- `GET /admin/devices/{id}`
- `POST /admin/devices/{id}/bind-user`
- `POST /admin/devices/{id}/unbind`
- `GET /admin/members`
- `POST /admin/members/grant`
- `POST /admin/members/{id}/cancel`
- `GET /admin/orders`
- `GET /admin/orders/{id}`
- `POST /admin/orders/{id}/mark-paid`
- `GET /admin/payment-callbacks`
- `GET /admin/payment-refunds`
- `POST /admin/refunds`
- `POST /admin/refunds/{id}/mark-success`
- `POST /admin/refunds/{id}/mark-failed`
- `GET /admin/launch-records`
- `GET /admin/launch-records/{id}`
- `GET /admin/adapter-reports`
- `GET /admin/adapter-reports/{id}`
- `POST /admin/adapter-reports/{id}/mark-processed`
- `POST /admin/adapter-reports/{id}/mark-failed`
- `GET /admin/logs/admin-operations`
- `GET /admin/logs/app-logins`
- `GET /admin/logs/launches`
- `GET /admin/logs/payment-events`
- `GET /admin/reports/overview`
- `GET /admin/reports/trend`
- `GET /admin/admin-users`
- `GET /admin/admin-users/{id}`
- `POST /admin/admin-users`
- `PUT /admin/admin-users/{id}`
- `PATCH /admin/admin-users/{id}/status`
- `POST /admin/admin-users/{id}/reset-password`
- `POST /admin/admin-users/me/change-password`

### 5.2 APP 统一接口

- `POST /api/auth/send-code`
- `POST /api/auth/login`
- `POST /api/device/register`
- `POST /api/device/launch`
- `GET /api/member/status`
- `POST /api/payment/create-order`
- `POST /api/payment/notify/{payChannel}`
- `POST /api/payment/dev/mark-paid`（仅开发调试，生产配置关闭）

### 5.3 适配层接口

- `POST /api/adapter/report`
- `GET /api/adapter/status`

---

## 6. 会员与支付流程

### 6.1 手机号登录

1. `POST /api/auth/send-code`：发送短信验证码；
2. `POST /api/auth/login`：校验验证码，生成 JWT，更新/创建统一用户账号；
3. 登录成功后，记录 `user_app_binding`，表明该用户已绑定到当前 APP；
4. 返回登录 Token 供后续接口使用。

### 6.2 设备码 VIP 判定

1. 客户端上报 `device_code` 与 `appId`；
2. 后端按 `appId + device_code` 查询或创建 `device_info`；
3. 后端按 `member_subject_type = DEVICE` 和 `device_id` 查询 `member_info`；
4. 返回 VIP 状态、是否过期、到期时间。
5. 对设备注册、会员查询、支付创建等接口按 `appId + device_code + ip` 做限频。

### 6.3 支付开通会员

1. 客户端选择套餐，请求 `POST /api/payment/create-order`；
2. 后端校验套餐和支付渠道配置，创建待支付订单，返回支付参数；
3. 客户端调用支付宝、微信或聚合支付；
4. 支付渠道回调 `POST /api/payment/notify/{payChannel}`；
5. 后端通过支付适配层完成验签、回调解析、幂等处理；
6. 后端更新订单、写入回调日志、开通或续期会员；
7. 如果是设备码 VIP APP，会员主体写为 `DEVICE` 并关联 `device_id`；如果是标准 APP，会员主体写为 `USER` 并关联 `user_id`。

### 6.4 退款处理

1. 管理员在后台针对已支付订单发起退款，生成 `payment_refund`；
2. 真实支付渠道接入前，可由后台手动确认退款成功或失败；
3. 退款成功后，订单累计 `refunded_amount_cents`，状态变为 `PARTIAL_REFUNDED` 或 `REFUNDED`；
4. 支付事件日志写入 `REFUND_CREATED`、`REFUND_SUCCESS` 或 `REFUND_FAILED`；
5. 如果当前会员记录来源于被退款订单，会员状态取消；如果已被后续续费覆盖，后续可扩展更精细的会员扣减策略。

### 6.5 会员状态查询

- `GET /api/member/status`：返回会员有效期、套餐、可用权益。

### 6.6 适配 APP 数据展示

1. 非统一认证/支付 APP 使用 `POST /api/adapter/report` 上报状态；
2. 后端把数据归档到 `adapter_report`；
3. 管理后台用统一报表查看运行情况、异常、业务指标。

---

## 7. 报表与数据展示

### 7.1 报表需求

- APP 级别用户/会员统计；
- 套餐销售与收入统计；
- 付费转化率；
- 活跃设备/启动次数；
- APP 登录成功/失败次数；
- 会员到期与续费数据；
- 支付回调成功率与异常订单；
- 付款、退款、人工补偿等支付事件统计；
- 后台管理员操作审计；
- 适配 APP 运行数据统计。

### 7.2 数据看板建议

- 总览面板：总 APP 数、总用户数、付费会员数、当日支付额、当日启动次数；
- 趋势面板：日/周/月新增用户、付费订单、会员增长；
- 渠道面板：按 APP、平台、渠道统计；
- 异常面板：回调失败、支付失败、设备异常上报。

### 7.3 数据仓库/统计策略

- 实时统计：直接查询 MySQL + Redis 缓存；
- 汇总统计：定时任务按日/周/月汇总写入统计表；
- 任务触发：订单完成后发送消息更新统计；
- 报表表：`daily_app_metrics`, `payment_summary`, `device_activity_summary`。

---

## 8. 技术选型细化

### 8.1 后端

- Java 版本：Java 8+，避免使用新版本语法以兼容旧服务器；
- Spring Boot：2.7.x；
- 建议框架：Spring Web、Spring Security、Spring Data JPA、Spring Cache、Spring Actuator；
- 支付 SDK：支付宝 Java SDK；
- 支付适配：定义统一 `PaymentProvider` 接口，支付宝、微信、聚合支付各自实现；
- JSON 处理：Jackson；
- 日志：Logback + Elasticsearch/Graylog（可选）。

### 8.2 数据库与缓存

- Main DB：MySQL 5.7+；
- 缓存：Redis 可选，当前验证码、nonce、限频可以先用本地内存实现，生产集群再替换为 Redis；
- 数据访问：Spring Data JPA + MyBatis（可选混用）；
- 事务：Spring 事务管理；
- 分库/分表：初期单库即可，未来可按 app_id 做逻辑分库。

### 8.3 前端与展示

- 管理后台：当前提供静态 HTML/CSS/JS 页面先跑通运营流程，后续可升级为 Vue 3 + Ant Design Vue 或 React + Ant Design；
- 可视化：ECharts；
- 数据表格：高级筛选、分页、导出；
- 权限：登录账户、角色管理，后台界面权限控制。

### 8.4 运行监控

- 接口监控：Spring Actuator；
- 日志告警：日志聚合 + 监控告警；
- 性能：慢查询、Redis 命中率、队列积压。

### 8.5 自动化回归测试

- 轻量烟测：`AdminApiSmokeTest` 覆盖后台登录、APP、支付配置、套餐、订单、回调、会员和退款主链路；
- 完整流程测试：`FullWorkflowIntegrationTest` 按业务顺序覆盖后台页面与鉴权、管理员维护、APP/套餐/支付配置维护、手机号跨 APP 统一用户、设备启动、会员、订单、退款、适配上报、日志、报表和演示数据；
- 测试数据库：集成测试强制使用 H2 内存库和 `ddl-auto=create-drop`，不会连接 MySQL，不会污染现有业务数据或推进现有自增索引；
- 外部支付：支付宝、微信、聚合支付实测需要真实商户和回调环境，自动化测试只验证平台内部订单、回调解析、幂等、会员生效和退款状态流转。

---

## 9. 扩展与适配层设计

### 9.1 适配层职责

- 支持“非统一登录/支付”的 APP 数据接入；
- 提供统一运行数据上报接口；
- 返回统一运行状态视图；
- 允许特判对应数据库或业务侧字段映射。

### 9.2 适配策略

- 每个适配 APP 维护 `adapter_config`：指定 `report_type`、`payload_schema`、`db_mapping_rule`；
- 后端通过 `app_id` 区分适配策略；
- 适配 APP 提供的 `source_id` 或 `external_id` 能映射到该 APP 的业务对象。

### 9.3 典型接入方式

- 业务上报接口：
  - `POST /api/adapter/report`
  - body 包含 `appId`, `sourceId`, `reportType`, `payload`
- 后端把上报数据落库并生成指标；
- 管理后台按 `appId` 展示“运行情况、日志、异常、健康状态”。

---

## 10. 关键设计考虑

### 10.1 统一业务与弹性扩展

- 对于“标准 APP”，提供完整登录/支付/会员；
- 对于“适配 APP”，提供统一数据展示与统计，而不强制登录/支付；
- 所有 APP 的核心指标可以统一上报与展示。

### 10.2 安全与隔离

- `appId` 与 `appSecret` 绑定请求，服务端只保存 `app_secret_hash`，明文 secret 仅创建或重置时返回；
- `appSecret` 支持重置和版本号，客户端请求携带 `appId`、时间戳、nonce、签名，服务端校验后处理；
- 1.0 阶段支持 `X-App-Id` + `X-App-Secret` 简化鉴权，也支持时间戳、nonce、签名模式，减少重放风险；
- 签名模式使用 `SHA-256(appSecret 明文)` 的十六进制结果作为 HMAC-SHA256 key，服务端只保存该 hash，不保存明文 secret；
- 支付回调入口不走 APP 鉴权，由对应支付渠道 Provider 做官方验签；
- 设备码 VIP APP 不强制设备码签名，但需要对设备注册、启动、会员查询、支付创建等接口做限频；
- 后台管理员 1.0 阶段可管理所有 APP，后续如需运营分权再增加 APP 级权限范围；
- 支付回调验签与幂等。

### 10.3 数据一致性与可追踪性

- 订单、会员、设备、启动记录要可追溯；
- 后台操作、APP 登录、启动、支付/退款事件要分别记录，避免把“打开 APP”和“登录成功”混为一类事件；
- 适配层上报数据要支持时间线查询；
- 数据展示与报表要支持按 APP、日期、渠道下钻。

### 10.4 系统演进路径

- 1.0：实现统一 APP 管理、手机号登录、设备码判定、支付订单、基本报表；
- 2.0：支持微信支付、自动续费、更多报表、适配层增强；
- 3.0：支持多租户、多数据源、智能风控、BI 数据仓库。

---

## 11. 后续建议

1. 确认本项目的首批接入 APP 类型与数量；
2. 确认支付宝、微信、聚合支付的具体服务商与回调参数格式；
3. 确定是否需要储值/功能包、会员等级、续费规则；
4. 确认是否需要业务端单独适配数据库字段映射；
5. 如果上述设计确认，我可以进一步生成：
   - 完整数据库建表脚本；
   - Spring Boot 项目结构；
   - 核心模块代码骨架；
   - API 文档与测试用例。
