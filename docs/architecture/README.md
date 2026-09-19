# 架构文档索引

本目录按职责拆分架构说明，避免将官网、后台、客户端和核心业务的约束混在一篇文档中。

| 文档 | 范围 |
|---|---|
| [系统总览](system-overview.md) | 系统边界、部署形态、模块依赖与演进方式 |
| [管理端架构](admin.md) | 管理后台、管理员身份、运营和内容管理 |
| [客户端 API 架构](app-api.md) | APP/小程序/受控服务调用的业务接口 |
| [官网与 Web 接口架构](public-web.md) | 官网展示、购买页和官网页面专用内容 API |
| [核心业务架构](core-domain.md) | 用户、APP、商品、会员、订单、支付等领域能力 |
| [官网内容运营与 AI 预留](content-and-ai.md) | 旧站功能迁移、内容域、官网后台与 AI 草稿助手设计 |
| [官网 UI 与发布设计](official-website-design.md) | 官网视觉、页面、后台体验、AI 预留及无感发布方案 |
| [生产发布运行手册](../deployment-runbook.md) | 环境、无感发布、验收、监控与回滚 |
| [平台能力与多 APP 配置](../backend-platform-app-design.md) | AI、支付、短信、邮件、验证码、搜索的配置分层 |

接口入口及模块导航见 [总接口文档](../api/overview.md)。
