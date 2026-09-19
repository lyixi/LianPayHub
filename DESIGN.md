# LYIXI Glin UI 重构设计规范

> 状态：设计基线，尚未实施页面代码  
> 目标版本：`@glinui/ui@0.1.1`、`@glinui/tokens@0.1.1`  
> 现有系统边界：Next.js 15 静态前端 + Spring Boot/MySQL 内容 API

## 1. 设计结论

这次不是给旧页面换皮，而是重建官网和运营后台的布局、排版、材质与交互。视觉方向为 **Liquid Product Atelier（液态产品工坊）**：界面像一组悬浮于有色环境光中的精密工作面板，玻璃用于表现层级和交互，不用于填满页面。

用户此前感受到的“大量灰色块”主要来自三件事：玻璃后方缺少可折射的内容、所有卡片采用相近不透明度、每个区块都被装进容器。本方案强制区分无框内容、轻玻璃、标准玻璃和真实折射，避免整页变成灰色卡片墙。

唯一的强视觉签名是 **Product Flow Rail（产品流光轨道）**：Hero 右侧不是普通截图卡，而是一条由产品真实界面片段、状态数据和能力节点组成的纵向透明轨道。指针移动只改变局部高光与轻微景深；滚动进入产品区时，轨道自然展开为产品矩阵。其他区域保持克制，避免动画和装饰相互争夺注意力。

### 1.1 产品与受众

- 公司：懒一夕智能科技（后台可修改站点名称）。
- 产品：写作天下、神奇弹幕及后续效率工具。
- 受众：内容创作者、职场用户、主播和直播运营团队。
- 官网任务：3 秒内解释“这家公司做什么、产品是否专业、下一步去哪”。
- 后台任务：运营人员能快速编辑、预览、发布、撤回和排序内容。

### 1.2 不做什么

- 不沿用旧版 Hero、旧工作台示意图、旧 Bento 尺寸和旧导航结构。
- 不把每个 section 做成卡片，不做卡片嵌套卡片。
- 不用 WebGL 作为基础材质，不保留全页持续渲染循环。
- 不把提示词中的 Prisma/SQLite 引入现有工程；数据库继续使用 MySQL。
- 不把产品、示例、指标、引用或首页文案写死在组件中。

## 2. Glin UI 真实能力基线

本规范以 npm 发布包 `0.1.1` 的类型声明和主题文件为准，不以宣传文案推测 API。

### 2.1 已确认可用

- `Card`：支持 `default | glass | frosted | outline | ghost | matte | liquid`。
- `Button`：支持上述主要表面及 `glow`。
- `Input`、`Textarea`、`Select`：支持 `glass`、`matte`、`liquid` 等变体。
- `DataTable`：自带搜索、列排序、分页、选择、空状态；不替代拖拽排序。
- `Sheet` / `Drawer`、`Modal`、`AlertDialog`、`DropdownMenu`、`Tabs`、`Toast`。
- `useLiquidGlass`：返回 `ref`、`svgFilter`、`style`、`filterId`、`isSupported`。
- `useLiquidGlass` 参数：`displacement`、`blur`、`saturate`、`profile: "squircle" | "convex"`、`disabled`。
- `@glinui/tokens/theme.css`：提供 `.dark` 主题、5 级玻璃层级和 reduced transparency/motion 回退。

### 2.2 必须纠正的提示词假设

- 当前发布包导出 76 个组件模块，不按“77 个”作为验收依据。
- `GlassCard` 只有 `size`，没有 `variant`；需要表面变体时使用 `Card variant="..."`。
- 包中没有统一的 `Form` 组件。表单由 React Hook Form + Zod 管理，Glin UI 提供字段和反馈组件。
- 不是所有组件都支持同一组 7 种变体；必须以各组件 TypeScript 类型为准。
- Glin UI 的深色选择器是 `.dark`，因此 `next-themes` 使用 `attribute="class"`。

## 3. 设计令牌

页面组件只使用语义 token。下列色值只允许出现在全局主题定义中，不允许散落在 JSX 或模块 CSS 中。

### 3.1 全局结构、排版和动效

```css
:root {
  --font-sans: "Inter", "Noto Sans SC", "PingFang SC", sans-serif;
  --font-mono: "JetBrains Mono", "SFMono-Regular", monospace;

  --content-max: 1280px;
  --content-reading: 760px;
  --page-gutter: clamp(20px, 5vw, 80px);
  --section-space: clamp(88px, 11vw, 152px);
  --admin-sidebar: 248px;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 24px;
  --space-6: 32px;
  --space-7: 48px;
  --space-8: 64px;
  --space-9: 96px;

  --radius-control: 12px;
  --radius-button: 14px;
  --radius-card: 22px;
  --radius-feature: 28px;
  --radius-overlay: 30px;

  --motion-fast: 160ms;
  --motion-normal: 280ms;
  --motion-enter: 620ms;
  --motion-page: 760ms;
  --ease-out: cubic-bezier(.16, 1, .3, 1);
  --ease-in-out: cubic-bezier(.65, 0, .35, 1);

  --type-hero: clamp(56px, 8.4vw, 124px);
  --type-display: clamp(40px, 5.4vw, 76px);
  --type-title: clamp(30px, 3.2vw, 48px);
  --type-heading: clamp(22px, 2vw, 30px);
  --type-body-lg: clamp(18px, 1.5vw, 21px);
  --type-body: 16px;
  --type-small: 14px;
  --type-meta: 12px;
}
```

字体不通过 viewport 线性放大；`clamp()` 只用于少量展示标题，控件和工作台文字保持稳定。中文标题字重 700，正文 400–500；全大写英文标签仅使用等宽字体。

### 3.2 浅色主题

浅色主题不是白底灰卡。底色为带少量冷青的雾白，主表面具有透明水晶感，正文区域允许直接落在背景上。

```css
:root,
.light {
  --color-background: oklch(0.975 0.009 215);
  --color-foreground: oklch(0.205 0.018 245);
  --color-surface: oklch(0.995 0.004 220);
  --color-border: oklch(0.86 0.018 225);
  --color-accent: oklch(0.57 0.17 256);
  --color-accent-foreground: oklch(0.985 0.004 220);

  --bg-base: oklch(0.975 0.009 215);
  --bg-canvas: oklch(0.955 0.020 214);
  --bg-surface: oklch(0.995 0.004 220 / .76);
  --bg-elevated: oklch(0.998 0.002 220 / .90);
  --text-primary: oklch(0.205 0.018 245);
  --text-secondary: oklch(0.45 0.025 238);
  --text-tertiary: oklch(0.61 0.023 232);
  --accent: oklch(0.57 0.17 256);
  --accent-hover: oklch(0.50 0.18 256);
  --accent-secondary: oklch(0.61 0.13 184);
  --accent-warm: oklch(0.72 0.13 70);
  --border-subtle: oklch(0.30 0.02 240 / .08);
  --border-strong: oklch(0.30 0.02 240 / .15);
  --focus-ring: oklch(0.57 0.17 256 / .24);
  --selection-bg: oklch(0.57 0.17 256 / .24);

  --ambient-violet: oklch(0.78 0.12 292 / .38);
  --ambient-cyan: oklch(0.84 0.11 190 / .42);
  --ambient-gold: oklch(0.90 0.10 88 / .34);
  --ambient-blue: oklch(0.76 0.11 238 / .28);
  --noise-opacity: .018;

  --glass-base-rgb: 255 255 255;
  --glass-adapt-surface-multiplier: .82;
  --glass-border-alpha: .24;
  --glass-border-strong-alpha: .40;
  --glass-refraction-top-alpha: .58;
  --shadow-panel: 0 1px 2px rgb(20 35 45 / .04),
                  0 10px 32px rgb(20 35 45 / .07),
                  0 32px 80px rgb(20 35 45 / .05);
  --shadow-float: 0 18px 60px rgb(38 65 86 / .13);
}
```

### 3.3 深色主题

深色主题使用墨蓝黑而非中性灰。玻璃中的暗部带蓝绿环境色，避免黑灰矩形堆叠。

```css
.dark {
  --color-background: oklch(0.155 0.022 255);
  --color-foreground: oklch(0.955 0.009 220);
  --color-surface: oklch(0.205 0.026 252);
  --color-border: oklch(0.34 0.028 245);
  --color-accent: oklch(0.72 0.17 263);
  --color-accent-foreground: oklch(0.15 0.02 255);

  --bg-base: oklch(0.155 0.022 255);
  --bg-canvas: oklch(0.185 0.032 254);
  --bg-surface: oklch(0.24 0.032 248 / .56);
  --bg-elevated: oklch(0.27 0.034 246 / .72);
  --text-primary: oklch(0.955 0.009 220);
  --text-secondary: oklch(0.74 0.025 225);
  --text-tertiary: oklch(0.57 0.027 230);
  --accent: oklch(0.72 0.17 263);
  --accent-hover: oklch(0.78 0.15 263);
  --accent-secondary: oklch(0.74 0.13 184);
  --accent-warm: oklch(0.78 0.13 72);
  --border-subtle: oklch(0.96 0.01 220 / .08);
  --border-strong: oklch(0.96 0.01 220 / .16);
  --focus-ring: oklch(0.72 0.17 263 / .30);
  --selection-bg: oklch(0.72 0.17 263 / .34);

  --ambient-violet: oklch(0.52 0.20 292 / .26);
  --ambient-cyan: oklch(0.58 0.15 190 / .22);
  --ambient-gold: oklch(0.60 0.13 76 / .16);
  --ambient-blue: oklch(0.42 0.16 250 / .24);
  --noise-opacity: .025;

  --glass-base-rgb: 8 13 24;
  --glass-adapt-surface-multiplier: .76;
  --glass-border-alpha: .13;
  --glass-border-strong-alpha: .22;
  --glass-refraction-top-alpha: .30;
  --shadow-panel: 0 1px 0 rgb(255 255 255 / .06) inset,
                  0 24px 70px rgb(0 0 0 / .34);
  --shadow-float: 0 28px 90px rgb(0 0 0 / .46);
}
```

导入 Glin token 后再声明这些覆盖项。保留库的 `--glass-1-*` 至 `--glass-5-*` 计算关系，只调整基础 RGB、透明度乘数和语义颜色。

## 4. Tailwind CSS v4 集成

Glin UI 的发布包在 JavaScript 中包含 Tailwind class 字符串。Tailwind v4 必须显式扫描该包，否则组件可能只显示无样式 HTML。

```css
/* web/app/globals.css */
@import "tailwindcss";
@import "@glinui/tokens/theme.css";
@source "../node_modules/@glinui/ui/dist";

@theme inline {
  --color-bg-base: var(--bg-base);
  --color-bg-canvas: var(--bg-canvas);
  --color-bg-surface: var(--bg-surface);
  --color-bg-elevated: var(--bg-elevated);
  --color-text-primary: var(--text-primary);
  --color-text-secondary: var(--text-secondary);
  --color-text-tertiary: var(--text-tertiary);
  --color-accent: var(--accent);
  --color-accent-hover: var(--accent-hover);
  --color-accent-secondary: var(--accent-secondary);
  --color-border-subtle: var(--border-subtle);
  --color-border-strong: var(--border-strong);
  --font-sans: var(--font-sans);
  --font-mono: var(--font-mono);
  --radius-control: var(--radius-control);
  --radius-card: var(--radius-card);
  --ease-product: var(--ease-out);
}
```

依赖在实施阶段固定版本：

```json
{
  "@glinui/ui": "0.1.1",
  "@glinui/tokens": "0.1.1"
}
```

## 5. 表面与海拔使用规则

海拔表达“谁盖在谁上面”，不是装饰强度。一个视口最多出现一个主折射面和两到四个标准玻璃面。

| 层级 | Glin token / 组件 | 用途 | 规则 |
|---|---|---|---|
| L0 | 无容器 / `ghost` | 大标题、正文、section 标题、Footer | 默认选择，制造呼吸感 |
| L1 | `.glass-1` / `outline` | 标签、筛选、行 hover | 不形成大面积灰底 |
| L2 | `.glass-2` / `glass` | 普通产品卡、后台指标、工具栏 | 每屏不超过 4 个大面 |
| L3 | `.glass-3` / `matte` | 表格外框、编辑区、侧栏 | 后台稳定工作层，不使用折射 |
| L4 | `.glass-4` / `frosted` | 导航、Sheet、Popover | 覆盖背景时保证文字对比度 |
| L5 | `.glass-5` / Modal | 对话框、确认层 | 仅瞬时出现 |
| Optical | `useLiquidGlass` | Hero 产品轨道、主产品卡、关键 CTA | 桌面同屏最多 2 个，移动端关闭 |

### 5.1 `GlassSurface` 项目封装

页面不得直接复制 `useLiquidGlass` 参数。统一封装负责支持检测、降级和性能策略：

```tsx
type GlassSurfaceProps = {
  level?: 1 | 2 | 3 | 4 | 5;
  optical?: boolean;
  profile?: "squircle" | "convex";
  interactive?: boolean;
  className?: string;
  children: React.ReactNode;
};
```

- `optical=false`：渲染普通元素并使用 Glin glass level。
- `optical=true` 且 Chromium 桌面、非 reduced-motion：使用 `useLiquidGlass`。
- 不支持 SVG backdrop filter：只使用 blur + saturate，内容不能消失。
- Hero 默认参数 `displacement: 22, blur: 14, saturate: 1.55`；按钮使用 `14/10/1.4`。
- 不在滚动列表、表格行、Logo 墙或后台指标卡使用 displacement。

## 6. 背景与材质

### 6.1 Ambient Field

全局背景不是四个随意漂移的圆形光斑，而是两层环境光：

1. 大尺度光场：两个椭圆色域，45–60 秒缓慢移动，仅改变 `transform`。
2. 产品轨道光：跟随 Hero 产品主题色，在 Hero 到产品区之间形成一条斜向光带。

不绘制边界可见的离散圆球。模糊 120–180px，透明度由主题 token 控制。`prefers-reduced-motion` 时完全静止。

### 6.2 噪点

使用一张小型静态 WebP 噪点平铺，透明度取 `--noise-opacity`。不使用每帧更新的 SVG turbulence。噪点层 `pointer-events: none`。

### 6.3 避免灰块的硬规则

- Section 背景默认透明；只有跨全宽叙事段可使用 `--bg-canvas` 色带。
- 同一网格中至少有一个无框项目，避免所有内容被同尺寸容器包围。
- 卡片内不再套装饰卡；内部层级用分隔线、留白和排版表达。
- 图片和产品 UI 是主视觉，不用灰色占位矩形模拟截图。
- 玻璃透明度必须让环境光可辨认，同时正文对比度达到 WCAG AA。

## 7. 前台页面架构

### 7.1 全局导航

- 桌面悬浮于顶部 16px，最大宽 1280px，L4 玻璃；Logo 左、导航中、操作右。
- 初始更透明；滚动 80px 后只提高表面不透明度和边框，不动态创建 filter。
- 导航项：产品、动态、关于、帮助；不存在的“定价”不先放入导航。
- 移动端：品牌 + 主题 + 菜单图标；菜单使用底部 Sheet。

### 7.2 Hero 与 Product Flow Rail

桌面为 7/5 非对称网格，首屏高度 `min(900px, 100svh)`，下一 section 保留可见提示。

左侧放真实站点 eyebrow、最多 14 个汉字的主标题、两行以内说明，以及精选产品和产品矩阵两个明确动作。

右侧 Product Flow Rail：

- 由后台精选产品真实数据生成，不写死“写作天下”。
- 一个 L2 基础工作面 + 一个 Optical 聚焦窗，不嵌套多个玻璃卡。
- 写作产品显示编辑器片段；直播产品显示弹幕流和延迟状态；其他类型显示可配置媒体主视觉。
- 鼠标移动只驱动 transform 和局部高光，最大倾斜 2deg。
- 移动端改为静态产品主视觉，不启用 pointer tracking 和 displacement。

### 7.3 场景条与产品矩阵

- 没有真实客户 Logo 时，不伪造 `TRUSTED BY`；改为“产品覆盖的工作场景”。
- 产品矩阵不使用固定三卡模板。第一项横向展开，后续按数据动态生成双列列表。
- 主产品允许一次 Optical 效果，其他产品使用 `Card variant="glass"` 或无框媒体布局。
- 产品卡显示名称、定位、真实能力标签、平台和明确动作。
- 路由统一为 `/product/[slug]`，与现有 Spring 路由兼容。

### 7.4 后续区块

- “统一能力底座”用一条系统轨道表达账户、支付、同步、AI 的关系，不用四张等宽特性卡。
- 数据区使用排版与分隔线，不给每个数字套卡片；数字来自站点设置。
- 动态区显示后台已发布文章，首篇为大图摘要，其余为紧凑列表。
- 客户评价只有存在真实后台数据时展示，禁止伪造姓名和职位。
- 联系 CTA 是全宽场景带，只有按钮使用 `liquid`；Footer 直接落在背景上。

### 7.5 详情、动态与状态

- 产品详情：标题与状态 → 真实主视觉 → 能力叙事 → 场景 → 评价 → 下载/购买。
- 功能段落左右交替，但不强制每项占一屏。
- 没有图片时使用品牌色排版构图，不使用灰色 mockup 占位块。
- 新闻正文最大宽 760px，玻璃只用于目录筛选或悬浮操作。
- API 失败提供“重新加载”；产品为空显示明确空状态，不回退到写死产品。

## 8. 官网运营后台 `/admin`

后台与官网共享 token，但不是官网卡片的复制。它是安静、密度适中、可扫描的工作台。

### 8.1 Shell 与 Dashboard

- 桌面左侧 248px L3 侧栏，主区为无框画布；顶部只保留面包屑、搜索、主题和账户。
- 小屏侧栏进入 Sheet；当前导航用 3px accent 竖线 + 轻背景。
- 导航：总览、产品、动态、首页内容、媒体、线索、设置。
- Dashboard 顶部为分栏发布摘要，不做四张同款大卡；趋势图独占 L3 工作面。
- Recharts 只动画一次；快捷动作是“新建产品”“发布动态”“编辑首页”。

### 8.2 产品与动态管理

- 使用真实 `DataTable` 完成搜索、排序、分页、选择和空状态。
- dnd-kit 仅负责产品顺序；拖拽结束只提交变化项并给出 Toast。
- 编辑使用右侧 `SheetContent variant="frosted" side="right"`，宽度 640–760px。
- 长表单分为基本信息、展示内容、下载与发布三个 Tabs，底部保存栏固定。
- 删除使用 `AlertDialog`，说明影响对象，成功后再移除行。
- 动态编辑首期沿用 Markdown/纯文本能力；未安装 MDX 编辑器前不虚构富文本功能。

### 8.3 内容、媒体、线索与设置

- 首页内容按前台真实 section 分组，并提供预览链接，不把几十个字段平铺一页。
- 媒体库使用图片网格 + 属性 Sheet；上传、失败、复制、替换和删除均有反馈。
- 线索用 DataTable 展示状态、来源、时间和联系信息，详情使用 Sheet。
- 设置拆分站点、SEO、社交与高级设置；维护模式和分析代码增加风险说明。

### 8.4 表单组合规范

Glin UI 不提供统一 `Form`，项目创建以下封装：

```text
FormField       Label + control + description + error
FormSection     字段组，不创建卡片嵌套
FormActions     保存、发布、撤回与脏数据状态
ImageField      媒体选择、预览、替换、alt
RepeaterField   功能、场景、评价的增删排序
SlugField       自动建议、手动修改、最终 URL
```

校验使用 Zod；提交状态包含 idle、saving、success、error。按钮在保存时显示“正在保存”，但宽度保持稳定。

## 9. 数据和接口边界

视觉重构不改变后端职责：

| 消费方 | 接口 | 说明 |
|---|---|---|
| 官网 | `GET /api/site/v1/site` | 品牌、Hero、能力、指标、联系与页脚 |
| 官网 | `GET /api/site/v1/products` | 已发布产品列表 |
| 官网 | `GET /api/site/v1/products/{slug}` | 产品详情 |
| 官网 | `GET /api/site/v1/articles` | 已发布动态 |
| 官网 | `POST /api/site/v1/leads` | 受控联系线索 |
| 运营后台 | `/admin/website/**` | 站点、产品、文章、媒体、线索 CRUD |
| API 后台 | `/console/**` | 独立于官网运营后台 |
| 程序 API | `/api/**` | 自有程序调用，不是第三方开放平台 |

实现时扩展现有 DTO 和 MySQL migration 以支持 features、useCases、testimonials、metrics 等结构；前台只渲染已发布数据。后台返回的 HTML 或脚本不得直接执行。

## 10. 组件命名规范

```text
app/      SiteThemeProvider, AmbientField, NoiseLayer, RouteTransition
site/     SiteHeader, MobileNavSheet, HeroSection, ProductFlowRail,
          ProductStage, ProductCard, CapabilityRail, MetricsStrip,
          JournalFeature, JournalList, ContactBand, SiteFooter
product/  ProductHero, ProductMediaStage, FeatureNarrative, UseCaseList,
          TestimonialQuote, ProductActionBand
admin/    AdminShell, AdminSidebar, AdminTopbar, PublishSummary,
          ContentDataTable, ProductEditorSheet, ArticleEditorSheet,
          HomepageSectionForm, MediaPicker, LeadDetailSheet, SettingsForm
ui/       GlassSurface, FormField, FormSection, FormActions,
          EmptyState, LoadingState, ErrorState
```

组件名必须描述业务职责。禁止 `Box`、`Wrapper`、`Thing`、`FancyCard` 等无意义命名。

## 11. 主题切换方案

```tsx
<ThemeProvider
  attribute="class"
  defaultTheme="system"
  enableSystem
  disableTransitionOnChange
>
  {children}
</ThemeProvider>
```

- `<html suppressHydrationWarning>` 避免 hydration 提示。
- 主题按钮使用 Lucide `Sun` / `Moon`，图标旋转 160ms；按钮为 12px squircle，不用 `rounded-full`。
- `disableTransitionOnChange` 防止 backdrop-filter 和大阴影同时插值闪烁；随后仅颜色和环境光做 280ms 过渡。
- 图表、图片亮度和环境光通过 `resolvedTheme` 更新。
- 首次静态 HTML 不输出错误主题图标；挂载前显示固定尺寸占位。

## 12. 动效与交互反馈

- 首屏只编排一次：导航淡入、标题分行上移、产品轨道轻推入场，总时长不超过 900ms。
- Section 入场使用 opacity + 16px translate，单组 stagger 不超过 240ms。
- hover 只动画 transform、opacity、border-color 和局部高光变量，不切换复杂 filter。
- 磁性按钮最大位移 4px，只用于 Hero 主 CTA。
- 页面转场使用 240ms 内容淡出/进入，不做全屏玻璃扫过。
- 所有保存、发布、复制、上传、删除和重排必须有可见反馈。
- reduced motion 下停用视差、轨道漂移、count-up 和 stagger，直接显示最终值。
- 触屏设备不渲染自定义光标；桌面自定义光标也不作为首期必需项。

## 13. 性能预算

- 首屏真实 SVG displacement：桌面最多 2 个，移动端 0 个。
- 任意视口 backdrop-filter 大面最多 5 个；后台列表最多 3 个。
- 环境动画只改变 `transform`，不动画 blur、filter、box-shadow。
- 不引入 Lenis；使用浏览器原生滚动并尊重 reduced motion。
- 不使用 WebGL Provider，不创建全页 Canvas，不监听整个 DOM 截图。
- 首页 LCP 主视觉优先使用本地优化图片；下一屏媒体 lazy load。
- 目标：Lighthouse Performance >= 85、Accessibility >= 95；无 hydration 或 console error。

## 14. 无障碍与响应式

- 所有正文与表面组合满足 WCAG AA；不依赖透明玻璃保证对比度。
- 键盘 focus 使用 `0 0 0 3px var(--focus-ring)`，不能只改变颜色。
- Dialog、Sheet、Dropdown 使用 Radix 的焦点管理和 Escape 关闭。
- 图标按钮具有中文 `aria-label` 和 Tooltip；装饰光场 `aria-hidden`。
- 导航 900px 收起，产品双列在 760px 合并，后台侧栏在 1024px 收起。
- 使用 `100svh`；320px 宽度无水平溢出；触控目标至少 44px。

## 15. 迁移顺序

1. 安装并锁定 Glin UI、tokens、Tailwind v4；建立主题和 `GlassSurface`，不改业务接口。
2. 删除前台对 `@el-gladiador/liquid-glass-react` 的引用，确认无引用后再卸载依赖。
3. 完全重建官网 Shell、Hero 和 Product Flow Rail，视觉验收后继续后续 section。
4. 重建产品详情、动态页面、错误/空状态并接真实 API。
5. 重建 `/admin` Shell、Dashboard、DataTable 和编辑 Sheet。
6. 完成产品、动态、首页、媒体、线索、设置的运营闭环。
7. 进行桌面/移动、深/浅主题、Chrome/Firefox/Safari 降级、键盘和性能验证。
8. 构建静态资源并部署到 Spring Boot，保留上一版静态产物以便快速回滚。

## 16. 验收标准

1. 首屏 3 秒内能识别公司、核心产品和主行动；Hero 不再沿用旧版构图。
2. 页面不存在成片相同灰色背景块；无框、轻玻璃、标准玻璃和折射层级肉眼可辨。
3. 深色与浅色主题分别具有清晰层次，不是简单反色。
4. 产品、示例、指标、文章、联系信息和页脚均来自后台，前端无业务内容兜底伪造。
5. `/admin` 可完成产品与动态的创建、编辑、排序、发布、撤回和删除，并提供操作反馈。
6. 不支持 SVG backdrop filter 或 reduced motion 时，功能和可读性完整。
7. 官网、后台在 320px 到宽屏无错位、遮挡、溢出和文字截断。
8. 无持续 WebGL/Canvas 循环；滚动、主题切换和 Drawer 操作无明显卡顿。
9. 不使用纯黑/纯白、Emoji 图标、紫色渐变按钮、默认阴影或卡片嵌套卡片。
10. `npm run build`、后端测试、接口回归和浏览器截图检查全部通过后才部署。
