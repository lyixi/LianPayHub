# 联付中枢总接口文档

本文档是接口入口、访问对象和鉴权方式的唯一总览；字段定义、示例和模块内部约定由各模块接口文档维护。接口的实时契约以 OpenAPI 为准：`/v3/api-docs`，可视化入口为 `/swagger-ui/index.html`。

## 接口分区

| 分区 | 路径 | 调用方 | 鉴权 | 当前状态 |
|---|---|---|---|---|
| 管理 API | `/admin/**` | 内部管理台、运营系统 | 管理员 JWT；生产应叠加 IP 白名单 | 已实现 |
| 公司程序 API | `/api/**` | 公司 APP、桌面端、小程序、受控服务 | 按接口使用用户 JWT、App Secret 或签名；不作为第三方开放平台 | 已实现 |
| 公开购买 API | `/public/**` | 已发布的购买落地页 | 公开；需按业务增加风控 | 已实现 |
| 支付回调 | `/api/payment/notify/**` | 支付渠道 | 渠道验签，不使用用户 JWT | 已实现 |
| 官网内容接口 | `/api/site/v1/**` | 公司官网页面 | 展示内容读取；线索提交受控开放 | 站点设置、产品、文章、线索已实现 |

`/admin/**` 不得被外部 APP 或官网前端直接调用；`/api/**` 也不应承担官网内容管理职责。

## 域名与部署建议

| 域名 | 路径/服务 | 用途 |
|---|---|---|
| `www.lyixi.com` / `lyixi.com` | 官网前端 | 品牌、产品展示、下载、资讯、SEO |
| `admin.lyixi.com` | `/console/` + `/admin/**` | 内部运营与配置 |
| `api.lyixi.com` | `/api/**` | 客户端业务调用与支付回调 |
| `www.lyixi.com` | `/api/site/v1/**` | 官网展示数据与受控线索提交；不作为合作方开放 API |

第一阶段可以由同一 Spring Boot 应用提供这些路径，按域名由 Nginx/API 网关分流；目录和权限必须先隔离。仅在流量、团队或安全隔离要求明确时再独立部署。

## 统一约定

- JSON 成功响应使用 `ApiResponse<T>`：`code = 0` 表示成功。
- 管理员接口：`Authorization: Bearer <admin-token>`。
- 用户态接口：`Authorization: Bearer <user-token>`。
- App 服务端鉴权支持 `X-App-Id` 搭配 `X-App-Secret`，或时间戳、nonce 与 HMAC 签名；完整规则见 [客户端 API 架构](../architecture/app-api.md)。
- 不在 URL、日志、浏览器代码或公开响应中暴露 `appSecret`、支付商户凭据、用户隐私数据。
- 新接口须归属一个分区；禁止新增没有业务归属的通用 `/api/public/**` 路径。

## 模块接口索引

| 模块 | 主路径 | 接口细节 |
|---|---|---|
| 管理后台 | `/admin/**` | [管理端架构](../architecture/admin.md) |
| 客户端业务 | `/api/auth`、`/api/device`、`/api/member`、`/api/payment`、`/api/sync` 等 | [客户端 API 架构](../architecture/app-api.md)；现有完整接入说明：[integration-api.md](../integration-api.md) |
| 购买落地页 | `/p/{slug}`、`/public/purchase-pages/**`、`/public/purchase-orders` | [官网与公开接口架构](../architecture/public-web.md) |
| 官网展示内容 | `/api/site/v1/**` | [官网与 Web 接口架构](../architecture/public-web.md) |
| 核心领域 | 不直接对外暴露 | [核心业务架构](../architecture/core-domain.md) |

## 版本和变更规则

当前接口保持现有路径兼容。对客户端、官网或第三方已使用的接口，破坏性变更必须新增 `/v2/...`，并保留旧版迁移窗口。每次接口变更同时更新 OpenAPI、对应模块文档和变更记录。
