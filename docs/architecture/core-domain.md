# 核心业务架构

## 核心领域

| 领域 | 主要职责 | 主要实体 |
|---|---|---|
| APP 与平台策略 | 多产品隔离、能力开关、配置覆盖 | `AppInfo`、`AppPlatformPolicy` |
| 用户与设备 | 账号、登录、绑定、设备状态 | `UserInfo`、`DeviceInfo`、`UserAppBinding` |
| 商品与会员 | 商品、方案、权益、有效期 | `ProductInfo`、`ProductPlan`、`PackageInfo`、`MemberInfo` |
| 交易与支付 | 下单、回调、退款、幂等与审计 | `PaymentOrder`、`PaymentCallbackLog`、`PaymentRefund` |
| 平台能力 | AI、短信、邮件、验证码、搜索 | 各 provider config 与 APP policy |
| 存储与配置 | 用户文件、配额、配置同步 | `UserFile`、`UserStorageQuota` |

## 依赖规则

```text
web（admin / api / public） -> service -> domain + repository
service -> 第三方适配器（支付、AI、短信、邮件、搜索、存储）
repository -> 数据库
```

Controller 不保存跨请求业务状态、不直接拼接数据库规则；`service` 负责权限后的领域校验、事务、状态流转和审计；`repository` 只负责持久化查询。公开、客户端和管理端均复用相同的 `service`。

## 关键一致性规则

- 订单金额、会员有效期、支付状态均以服务端计算和状态流转为准，绝不信任前端提交结果。
- 支付回调必须验签、可幂等重放且记录事件；退款改变订单/会员状态时必须可审计。
- 每条业务数据以 `appId` 作为隔离维度；跨 APP 查询或写入必须显式授权。
- 平台 provider 凭据属于平台层，APP 只保存可用范围和策略覆盖；详细设计见 [平台能力与多 APP 配置](../backend-platform-app-design.md)。
