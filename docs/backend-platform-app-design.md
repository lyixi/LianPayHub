# 后台平台与多APP配置设计

## 1. 设计目标

联付中枢要作为多个 APP 的统一后台，既保留平台能力的统一管理，又允许每个 APP 自定义会员和接入策略。

目标是把后台分成两层：

- 平台层：统一管理 AI、支付、短信、邮件、验证码、搜索等第三方能力。
- APP 层：每个 APP 单独配置会员套餐、默认权益、通道可用范围和风控策略。

## 2. 分层模型

### 2.1 平台层

平台层只负责“能力提供者”：

- `ai`：AI provider、模型路由、密钥、额度入口
- `payment`：支付宝、微信、聚合支付、易支付、PayJS、虎皮椒、手动确认
- `sms`：阿里云、腾讯云、华为云、火山、云片、mock
- `email`：SMTP、SendGrid、Mailgun、阿里云邮件、AWS SES、console
- `captcha`：local、Turnstile、hCaptcha、极验、腾讯云、网易易盾
- `search`：博查等外部搜索服务

平台层的核心字段建议统一成：

- `category`
- `providerCode`
- `displayName`
- `status`
- `isDefault`
- `configJson`
- `credentialJson`
- `capabilities`
- `secretMask`

### 2.2 APP 层

APP 层只负责“业务策略”和“覆盖关系”：

- 默认会员主体：账号或设备
- 可用支付通道：按 APP 限制
- 默认 AI 平台：按 APP 选择
- 是否启用用户 AI Key
- AI 点数、日限额、月限额
- 短信验证码策略：是否必需、默认模板、冷却时间、错误上限
- 验证码策略：是否启用、默认 provider、是否登录必过
- 搜索策略：是否允许、点数单价、最大条数、可搜范围

## 3. 统一平台配置设计

当前实现已新增通用 APP 覆盖层：

- 表：`app_platform_policy`
- 管理接口：`GET /admin/app-platform-policies`、`POST /admin/app-platform-policies`
- 支持分类：`AI`、`PAYMENT`、`SMS`、`EMAIL`、`CAPTCHA`
- 统一字段：`appId`、`category`、`enabled`、`providerCode`、`configJson`、`credentialJson`、`policyJson`

运行时已经接入：

- 支付下单：读取 `PAYMENT.policyJson.defaultPayChannel`，请求未传 `payChannel` 时按 APP 默认渠道下单。
- AI 网关：读取 `AI.enabled/providerCode`，支持 APP 级禁用 AI 或指定默认供应商。
- 用户 AI Key 自动发放：读取 `AI.enabled/providerCode`。
- 短信验证码：读取 `SMS.enabled/providerCode/policyJson.cooldownSeconds/expireMinutes`。
- 独立验证码：`POST /api/captcha/challenge`、`POST /api/captcha/verify`，读取 `CAPTCHA.policyJson.ttlSeconds/length/maxAttempts/debugReturnCode`。
- 搜索平台：`search_platform_config`，用于博查等第三方检索服务的统一配置。
- 统一业务搜索：`GET /api/search`、`GET /admin/platform-search`，用于本系统数据检索，和第三方搜索平台配置分离。

现有 provider 表继续作为平台层凭据来源：`ai_provider_config`、`app_ai_provider_setting`、`payment_channel_config`、`notification_channel_config`、`search_platform_config`。`app_platform_policy` 只表达 APP 覆盖和策略，不替代 provider 凭据表。

### 3.1 AI

平台层保存：

- provider 基础信息
- baseUrl / consoleBaseUrl
- 模型或分组配置
- 调用凭据

APP 层覆盖：

- 默认 provider
- 默认 quota
- daily limit
- 是否允许用户单独配置 key
- key group / route

### 3.2 支付

平台层保存：

- 支付渠道 provider 配置
- 真实商户参数
- 回调地址模板
- 测试能力

APP 层覆盖：

- 该 APP 允许使用哪些支付渠道
- 默认支付渠道
- 订单过期时长
- 套餐是否允许折扣或定制价
- 会员主体是 user 还是 device

已实现策略示例：

```json
{"defaultPayChannel":"WECHAT"}
```

### 3.3 短信

平台层保存：

- provider 基础配置与密钥
- 模板参数名
- 发送能力和测试能力

APP 层覆盖：

- 是否强制手机号登录
- 登录短信是否必发
- 发送冷却时间
- 错误次数上限
- 是否允许 mock

已实现策略示例：

```json
{"cooldownSeconds":60,"expireMinutes":5}
```

### 3.4 邮件

平台层保存：

- SMTP / API 邮件 provider
- 发件人信息
- 凭据与 endpoint

APP 层覆盖：

- 通知邮件是否启用
- 哪些业务事件触发邮件
- 默认模板和收件人策略

### 3.5 验证码

平台层保存：

- 本地图片验证码
- Turnstile / hCaptcha / 极验 / 腾讯云 / 网易易盾

APP 层覆盖：

- 登录页是否启用验证码
- 管理后台是否启用验证码
- 连续失败后的强制验证码策略

已实现策略示例：

```json
{"ttlSeconds":300,"length":6,"maxAttempts":5,"debugReturnCode":false}
```

### 3.6 搜索

平台层保存：

- 搜索 provider
- API baseUrl
- 余额或点数查询能力
- 请求限制

APP 层覆盖：

- 是否允许搜索
- 默认搜索 provider
- 单次搜索消耗点数
- 是否允许公开搜索、仅后台搜索或仅特定房间搜索

已实现策略示例：

```json
{"maxLimit":50}
```

## 4. 会员与权益设计

每个 APP 维持独立会员体系，但统一到同一套实体模型：

- `product_info`：APP 内商品定义
- `product_plan`：商品下的可购买方案
- `member_info`：实际会员状态
- `payment_order`：支付订单

会员设计原则：

- 账号型 APP 使用 `user_id` 作为会员主体
- 设备型 APP 使用 `device_id` 作为会员主体
- 不同 APP 的权益字段可以不同，但都映射到各自 APP 的 product / plan
- `benefitsText` 只做展示，不替代结构化权益字段

建议的 APP 级定制项：

- 会员有效期
- 续费是否叠加
- AI 点数赠送
- 是否包含搜索额度
- 是否包含短信/邮件通知权益
- 是否允许设备迁移

## 5. 后台页面设计

后台建议拆成四个一级区：

1. 平台配置
2. APP 配置
3. 会员与商品
4. 运营与审计

### 5.1 平台配置页

顶部按分类展示：

- AI
- 支付
- 短信
- 邮件
- 验证码
- 搜索

每个 provider 卡片都要支持：

- 查看详情
- 修改配置
- 启停
- 设为默认
- 保存并测试

### 5.2 APP 配置页

每个 APP 详情页至少展示：

- 基础信息
- APP 类型
- 会员主体类型
- 支付通道覆盖
- AI 覆盖
- 短信策略
- 验证码策略
- 搜索策略
- 套餐/商品/方案

## 6. 配置生效规则

优先级建议：

1. 请求显式参数
2. APP 级覆盖
3. 平台默认 provider
4. 系统内置 fallback

所有敏感字段只允许后台回显掩码，不允许直接返回明文。

## 7. 兼容与迁移

现有结构可以平滑迁移：

- `payment_channel_config` 兼容为支付平台层 provider 的 APP 级实例化视图
- `notification_channel_config` 兼容为短信/邮件平台层 provider 的单表视图
- `ai_provider_config`、`app_ai_provider_setting`、`user_ai_credential` 继续保留，但要收敛到统一配置解释层
- `package_info`、`product_info`、`product_plan`、`member_info` 作为会员与权益主模型

## 8. 文档同步原则

本仓库的实现说明应与此设计一致，避免出现：

- README 写“已实现”，设计文档写“目标态”
- 后台页面和 API 命名不一致
- 平台层和 APP 层职责混淆
