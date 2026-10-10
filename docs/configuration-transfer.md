# 配置导入导出

在业务后台 `/console/` 的「诊断工具」中选择「导出所有配置」，在另一台运行本服务的设备上选择该 ZIP 备份，预览后确认导入。旧版 JSON 配置包仍可导入，但不含资源文件。

## 范围

配置包包含应用、AI 平台、搜索平台、通知渠道、支付渠道、应用平台策略、应用 AI 参数、套餐、商品、商品规格、购买页、官网产品、文章、页面、公司信息及站点设置、媒体库记录。包括密钥和应用凭证，文件必须作为敏感备份保管。

ZIP 同时包含媒体库的实际资源文件：产品封面、轮播图、文章图片、公司图片、安装包和其他已上传的官网资源。恢复时更新已登记媒体的 URL 引用，使用本机资源访问地址。资源缺失时拒绝导出或恢复，避免生成不完整备份。外部链接仅保留地址，不下载外部网站文件。目前使用项目现有的本地存储后端。

用户、设备、会员、订单、退款、用户私有配置、日志、管理员账号、用户头像和用户云同步文件不包含在配置包中。数据库连接、服务端环境变量、JWT 签名密钥和本地浏览器设置也需要在设备上单独配置。

## 重复与冲突

- 使用业务标识匹配，不使用来源设备的数据库 ID。应用按 appId，平台按 providerCode，支付按 appId + payChannel，通知按 channelType + providerCode，策略按 appId + category，应用 AI 按 appId + providerCode，商品按 appId + productCode，规格按匹配后的商品 + planCode，页面及官网内容按 slug，站点设置为单例。
- 旧套餐没有独立编码，按 appId + packageType + packageName 匹配。同一设备存在多个该标识相同的套餐时拒绝整批导入，需要先消除歧义；修改套餐名称会被视为新套餐。
- 内容完全相同：自动跳过，不改更新时间，不重复新增。
- 来源独有：新增，商品规格的商品 ID 和旧套餐 ID 自动映射到本机。
- 内容有差异：默认保留本机；选择「使用导入配置」时覆盖，包括密钥。预览显示数量和冲突标识，不显示密钥。
- 媒体按 storageKey 匹配，资源文件逐字节比较，相同跳过；同一路径内容不同，使用同一批次的冲突策略。整个批次只在一个确认弹窗选择策略，不逐项弹窗。不同 storageKey 的资源作为不同记录合并。
- 官网产品相册和下载列表中的媒体 ID 自动映射到本机；对应媒体未包含在配置包中时拒绝导入。
- 本机独有：保留。源设备删除的记录不会导致本机删除。
- 文件中重复标识、未知字段、无效版本、缺失关联或非法字段：拒绝导入，已写入的数据整体回滚。

这是手动合并，不自动根据时间决定冲突，也不进行后台实时同步。两台设备可先使用「保留本机」互相导入，实现新增配置的合并；发生冲突时，确定以哪台设备为准，在另一台选择「使用导入配置」。预览之后发生的本机改动以确认导入时的实际数据重新计算，完成页显示实际结果。

## API

- `GET /admin/configuration-transfer/export`：下载版本 1 JSON 配置包。
- `GET /admin/configuration-transfer/backup`：下载包含配置和实际资源文件的 ZIP 备份。
- `POST /admin/configuration-transfer/restore?preview=true&strategy=KEEP_LOCAL`：multipart/form-data 的 file 字段上传 ZIP，预览；preview=false 执行恢复。
- `POST /admin/configuration-transfer/import?preview=true&strategy=KEEP_LOCAL`：预览，正文为配置包。
- `POST /admin/configuration-transfer/import?preview=false&strategy=OVERWRITE`：执行导入。

所有接口需要管理员 Bearer token。导入默认只预览、默认保留本机。可以省略不需要导入的 section；导入规格时需要同时提供对应商品和非空旧套餐关联的套餐 section。配置值严格按实体字段校验，单个 section 最多 10,000 条。审计仅记录配置导入动作，不记录配置包正文。
