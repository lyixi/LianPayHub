#!/usr/bin/env node

/**
 * Import the useful software catalogue from the former lyixi.com site.
 *
 * The importer is deliberately idempotent:
 * - products are matched by slug;
 * - media are matched by their legacy source marker;
 * - existing publish states are preserved;
 * - existing products are only enriched when --update-existing is supplied.
 *
 * Usage:
 *   node scripts/import-legacy-products.mjs --update-existing
 *
 * Optional environment variables:
 *   LIANPAYHUB_BASE_URL=http://127.0.0.1:8888
 *   LIANPAYHUB_ADMIN_USERNAME=admin
 *   LIANPAYHUB_ADMIN_PASSWORD=admin123456
 */

const baseUrl = (process.env.LIANPAYHUB_BASE_URL || "http://127.0.0.1:8888").replace(/\/$/, "");
const username = process.env.LIANPAYHUB_ADMIN_USERNAME || "admin";
const password = process.env.LIANPAYHUB_ADMIN_PASSWORD || "admin123456";
const updateExisting = process.argv.includes("--update-existing");
const legacyOrigin = "https://lyixi.com";

const products = [
  {
    legacyId: 100,
    slug: "writing-world",
    name: "写作天下",
    category: "智能写作",
    tagline: "为长篇创作者打造的智能写作环境",
    summary: "集作品目录、资料管理、智能标点、名片库、云端同步与创作辅助于一体，让复杂的长篇写作保持清晰和流畅。",
    cover: "/media/20210803143631NG.jpg",
    accentColor: "#E97850",
    sortOrder: 10,
    fallbackFeatures: ["自由目录与作品管理", "智能标点与写作辅助", "人物名片库与全书检索", "云端同步与章节快照"]
  },
  {
    legacyId: 101,
    slug: "code-writing",
    name: "码字风云",
    category: "智能写作",
    tagline: "随时进入状态的移动写作工具",
    summary: "面向移动端创作者的智能写作软件，把作品、章节和灵感装进随身的轻量工作台。",
    cover: "/media/20210328001659OW.png",
    accentColor: "#6E83D6",
    sortOrder: 20,
    fallbackFeatures: ["移动端章节创作", "作品与目录管理", "灵感随手记录", "轻量流畅的编辑体验"]
  },
  {
    legacyId: 102,
    slug: "lazy-key",
    name: "懒键",
    category: "智能写作",
    tagline: "重新定义 VS Code 的输入效率",
    summary: "根据编辑上下文自动输入合适内容，减少重复击键，让代码与文字输入更自然。",
    cover: "/media/20210328001718yn.gif",
    accentColor: "#42A58C",
    sortOrder: 30,
    fallbackFeatures: ["上下文智能输入", "减少重复击键", "融入 VS Code 工作流", "轻量而不打断思路"]
  },
  {
    legacyId: 104,
    slug: "snow-music",
    name: "雪以音乐",
    category: "媒体工具",
    tagline: "跨平台搜索与播放的轻量点歌软件",
    summary: "同时支持网易云音乐、QQ 音乐和咪咕音乐，以更少的步骤完成搜索、点歌与播放。",
    cover: "/media/202103240912083b.png",
    accentColor: "#D56D77",
    sortOrder: 40,
    fallbackFeatures: ["多音乐平台聚合", "快速搜索与点歌", "轻量播放界面", "减少平台切换成本"]
  },
  {
    legacyId: 105,
    slug: "time-screenshot",
    name: "鸽鸽子时光截图",
    category: "效率办公",
    tagline: "持续保存屏幕信息的时光回溯工具",
    summary: "自动记录关键屏幕画面，需要时快速回看，减少信息稍纵即逝带来的措手不及。",
    cover: "/media/20210803144028g4.jpg",
    accentColor: "#C88A45",
    sortOrder: 50,
    fallbackFeatures: ["自动持续截图", "按时间快速回看", "降低信息遗漏", "适合临时操作追溯"]
  },
  {
    legacyId: 108,
    slug: "cloud-note",
    name: "云笺",
    category: "智能写作",
    tagline: "可分享、可扩展的云端笔记",
    summary: "支持文字记录、页面分享、HTML、短网址与实时同步，并提供接口能力连接更多工作流。",
    cover: "/media/20210328001809FK.png",
    accentColor: "#5B91D8",
    sortOrder: 60,
    fallbackFeatures: ["云端实时同步", "笔记页面分享", "HTML 与短网址", "开放接口能力"]
  },
  {
    legacyId: 110,
    slug: "stay-awake",
    name: "不要休眠",
    category: "效率办公",
    tagline: "一键保持电脑清醒",
    summary: "在演示、下载或长时间任务期间临时禁止电脑睡眠与休眠，用完即走，不修改复杂系统设置。",
    cover: "/media/202108031439496i.jpg",
    accentColor: "#D07C52",
    sortOrder: 70,
    fallbackFeatures: ["临时禁止睡眠", "临时禁止休眠", "一键启用与恢复", "适合长任务和演示"]
  },
  {
    legacyId: 113,
    slug: "folder-sync",
    name: "文件夹双向同步",
    category: "效率办公",
    tagline: "让两个文件夹始终保持一致",
    summary: "在本地文件夹、公共盘等位置之间执行双向定时同步，降低人工复制和版本遗漏。",
    cover: "/media/20210406213456G1.png",
    accentColor: "#4A9C98",
    sortOrder: 80,
    fallbackFeatures: ["文件夹双向同步", "定时执行任务", "适配本地盘与公共盘", "减少重复复制"]
  },
  {
    legacyId: 114,
    slug: "list-sorter",
    name: "列表信息分拣",
    category: "效率办公",
    tagline: "把散乱信息快速整理成表格",
    summary: "识别具有一定规则的列表信息并自动分类整理，输出更清晰、可继续处理的表格数据。",
    cover: "/media/20210415210654Mg.jpg",
    accentColor: "#8A78C2",
    sortOrder: 90,
    fallbackFeatures: ["规则信息识别", "自动分类整理", "生成结构化表格", "适合批量名单处理"]
  },
  {
    legacyId: 115,
    slug: "meal-count",
    name: "放学点餐人数统计",
    category: "效率办公",
    tagline: "根据课程表快速统计餐点人数",
    summary: "按课程表与放学安排计算需要订餐的人数，并导出 Excel 数据，减少重复核对。",
    cover: "/media/20210415204004Hf.jpg",
    accentColor: "#C59646",
    sortOrder: 100,
    fallbackFeatures: ["读取课程安排", "自动统计餐点人数", "导出 Excel 数据", "降低人工核对成本"]
  },
  {
    legacyId: 130,
    slug: "floating-universe",
    name: "悬浮宇宙",
    category: "效率办公",
    tagline: "随时呼出的无限桌面画布",
    summary: "在桌面悬浮空间中放置文件、文件夹、网址、文字和图片，鼠标贴近屏幕顶边即可快速呼出。",
    cover: "/media/20210721100806uP.png",
    accentColor: "#4A82D9",
    sortOrder: 110,
    fallbackFeatures: ["无限尺寸桌面画布", "文件与网址快捷入口", "文字、图片和 HTML 内容", "贴近屏幕顶边快速呼出"]
  },
  {
    slug: "shenqi-danmu",
    name: "神奇弹幕",
    category: "直播互动",
    tagline: "为直播现场打造的实时互动中枢",
    summary: "聚合多平台弹幕，并提供弹幕特效、关键词抽奖和实时数据看板，兼顾低延迟、高并发与皮肤定制。",
    accentColor: "#55AFA5",
    sortOrder: 120,
    fallbackFeatures: ["多平台弹幕聚合", "弹幕特效与可定制皮肤", "关键词抽奖", "低延迟实时数据看板"]
  }
];

function fallbackContent(product) {
  return `<h2>产品简介</h2><p>${product.summary}</p><h2>核心能力</h2><ul>${product.fallbackFeatures.map((feature) => `<li><strong>${feature}</strong></li>`).join("")}</ul><blockquote><p>该产品资料由旧版官网迁移，当前版本、支持平台和下载文件请由运营人员确认后再发布。</p></blockquote>`;
}

async function request(path, token, options = {}) {
  const headers = { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) headers["Content-Type"] = "application/json";
  const response = await fetch(`${baseUrl}${path}`, { ...options, headers });
  const body = await response.json().catch(() => null);
  if (!response.ok || !body || body.code !== 0) throw new Error(body?.message || `${options.method || "GET"} ${path} failed (${response.status})`);
  return body.data;
}

function absoluteLegacyUrl(value) {
  if (!value) return "";
  if (/^https?:\/\//i.test(value)) return value;
  if (value.startsWith("//")) return `https:${value}`;
  return new URL(value, legacyOrigin).href;
}

function extractProductContent(html) {
  const start = html.search(/<div\s+class=["']product_content["'][^>]*>/i);
  if (start < 0) return "";
  const bodyStart = html.indexOf(">", start) + 1;
  const candidates = [html.indexOf("function/like.php", bodyStart), html.indexOf('id="comments_box"', bodyStart), html.indexOf("id='comments_box'", bodyStart)].filter((index) => index > bodyStart);
  let end = candidates.length ? Math.min(...candidates) : html.length;
  const scriptStart = html.lastIndexOf("<script", end);
  if (scriptStart > bodyStart) end = scriptStart;
  return html.slice(bodyStart, end);
}

function extractImageUrls(html) {
  const urls = [];
  const matcher = /(?:src\s*=\s*["']([^"']+)["']|background-image\s*:\s*url\(\s*["']?([^"')\s]+))/gi;
  for (const match of html.matchAll(matcher)) {
    const raw = match[1] || match[2];
    if (!raw || raw.startsWith("data:") || raw.endsWith(".svg")) continue;
    const url = absoluteLegacyUrl(raw);
    if (url.startsWith(`${legacyOrigin}/media/`) || url.startsWith(`${legacyOrigin}/ueditor/`)) urls.push(url);
  }
  return [...new Set(urls)];
}

function readAttribute(attributes, name) {
  const match = attributes.match(new RegExp(`\\b${name}\\s*=\\s*(?:["']([^"']*)["']|([^\\s>]+))`, "i"));
  return match ? (match[1] ?? match[2] ?? "") : "";
}

function escapeAttribute(value) {
  return String(value).replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;");
}

function sanitizeLegacyHtml(html, assetUrls) {
  const cleaned = html
    .replace(/<!--[\s\S]*?-->/g, "")
    .replace(/<script\b[\s\S]*?<\/script>/gi, "")
    .replace(/<style\b[\s\S]*?<\/style>/gi, "")
    .replace(/<svg\b[\s\S]*?<\/svg>/gi, "")
    .replace(/<a\b[^>]*class=["'][^"']*anchor[^"']*["'][^>]*>\s*<\/a>/gi, "");
  const allowed = new Set(["h1", "h2", "h3", "h4", "p", "ul", "ol", "li", "blockquote", "a", "img", "strong", "em", "br", "hr", "code", "pre"]);
  return cleaned.replace(/<\/?([a-z0-9]+)\b([^>]*)>/gi, (whole, rawTag, attributes) => {
    let tag = rawTag.toLowerCase();
    const closing = whole.startsWith("</");
    if (!allowed.has(tag)) return "";
    if (tag === "h1") tag = "h2";
    if (closing) return `</${tag}>`;
    if (tag === "a") {
      const rawHref = readAttribute(attributes, "href");
      if (!rawHref || /^javascript:/i.test(rawHref)) return "<a>";
      const href = rawHref.startsWith("#") || /^mailto:/i.test(rawHref) ? rawHref : absoluteLegacyUrl(rawHref);
      return `<a href="${escapeAttribute(href)}" target="_blank" rel="noopener noreferrer">`;
    }
    if (tag === "img") {
      const source = absoluteLegacyUrl(readAttribute(attributes, "src"));
      const localUrl = assetUrls.get(source);
      if (!localUrl) return "";
      const alt = readAttribute(attributes, "alt") || readAttribute(attributes, "title") || "产品截图";
      return `<img src="${escapeAttribute(localUrl)}" alt="${escapeAttribute(alt)}">`;
    }
    return ["br", "hr"].includes(tag) ? `<${tag}>` : `<${tag}>`;
  }).replace(/\s{2,}/g, " ").trim();
}

async function fetchLegacyPage(product) {
  if (!product.legacyId) return { html: "", content: "" };
  const response = await fetch(`${legacyOrigin}/?type=productinfo&S_id=${product.legacyId}`);
  if (!response.ok) throw new Error(`旧站产品页 ${product.legacyId} 返回 ${response.status}`);
  const html = await response.text();
  return { html, content: extractProductContent(html) };
}

async function uploadLegacyAsset(url, product, token, mediaBySource) {
  const sourceMarker = `旧站迁移来源：${url}`;
  if (mediaBySource.has(sourceMarker)) return mediaBySource.get(sourceMarker);
  const response = await fetch(url);
  if (!response.ok) throw new Error(`${url} 返回 ${response.status}`);
  const bytes = await response.arrayBuffer();
  const contentType = (response.headers.get("content-type") || "application/octet-stream").split(";")[0];
  const fileName = decodeURIComponent(new URL(url).pathname.split("/").pop() || "legacy-asset");
  const form = new FormData();
  form.append("file", new Blob([bytes], { type: contentType }), fileName);
  form.append("title", `${product.name} · ${fileName}`);
  form.append("altText", `${product.name} 产品图片`);
  form.append("description", sourceMarker);
  const media = await request("/admin/website/media", token, { method: "POST", body: form });
  mediaBySource.set(sourceMarker, media);
  return media;
}

async function importProduct(product, token, currentProducts, mediaBySource) {
  const existing = currentProducts.find((item) => item.slug === product.slug);
  if (existing && !updateExisting) {
    console.log(`SKIP   ${product.name}（已存在，使用 --update-existing 可补充资料）`);
    return existing;
  }

  let legacyPage = { html: "", content: "" };
  try {
    legacyPage = await fetchLegacyPage(product);
  } catch (error) {
    console.warn(`WARN   ${product.name}：无法读取旧站正文，使用整理后的基础资料；${error.message}`);
  }

  const contentImages = extractImageUrls(legacyPage.content);
  const requestedAssets = [...new Set([product.cover && absoluteLegacyUrl(product.cover), ...contentImages].filter(Boolean))];
  const uploaded = [];
  for (const url of requestedAssets) {
    try {
      uploaded.push({ source: url, media: await uploadLegacyAsset(url, product, token, mediaBySource) });
    } catch (error) {
      console.warn(`WARN   ${product.name}：图片迁移失败 ${url}；${error.message}`);
    }
  }

  const assetUrls = new Map(uploaded.map((item) => [item.source, item.media.url]));
  const sanitized = legacyPage.content ? sanitizeLegacyHtml(legacyPage.content, assetUrls) : "";
  const coverMedia = uploaded.find((item) => item.source === absoluteLegacyUrl(product.cover))?.media;
  const existingGallery = (() => { try { return JSON.parse(existing?.mediaJson || "[]"); } catch { return []; } })();
  const migratedGallery = uploaded.map(({ media }, index) => ({ id: media.id, url: media.url, title: media.title, altText: media.altText, sortOrder: index }));
  const gallery = [...migratedGallery, ...existingGallery.filter((asset) => !migratedGallery.some((candidate) => candidate.url === asset.url))]
    .map((asset, index) => ({ ...asset, sortOrder: index }));

  const payload = {
    ...(existing || {}),
    slug: product.slug,
    appId: existing?.appId || null,
    name: product.name,
    tagline: product.tagline,
    summary: product.summary,
    accentColor: product.accentColor,
    coverUrl: coverMedia?.url || existing?.coverUrl || "",
    downloadUrl: existing?.downloadUrl?.startsWith("#") ? "" : (existing?.downloadUrl || ""),
    downloadJson: existing?.downloadJson || "[]",
    purchaseUrl: existing?.purchaseUrl?.startsWith("#") ? "" : (existing?.purchaseUrl || ""),
    content: sanitized || existing?.content || fallbackContent(product),
    mediaJson: JSON.stringify(gallery),
    sortOrder: product.sortOrder
  };

  const saved = await request(existing ? `/admin/website/products/${existing.id}` : "/admin/website/products", token, {
    method: existing ? "PUT" : "POST",
    body: JSON.stringify(payload)
  });
  console.log(`${existing ? "UPDATE" : "CREATE"} ${product.name} / ${product.slug} / ${saved.status}`);
  return saved;
}

async function main() {
  console.log(`连接 ${baseUrl}，开始迁移旧站产品（更新已有产品：${updateExisting ? "是" : "否"}）`);
  const login = await request("/admin/auth/login", "", { method: "POST", body: JSON.stringify({ username, password }) });
  const token = login.token;
  const [currentProducts, media] = await Promise.all([
    request("/admin/website/products", token),
    request("/admin/website/media", token)
  ]);
  const mediaBySource = new Map(media.filter((item) => item.description?.startsWith("旧站迁移来源：")).map((item) => [item.description, item]));

  const imported = [];
  for (const product of products) imported.push(await importProduct(product, token, currentProducts, mediaBySource));

  const refreshed = await request("/admin/website/products", token);
  console.log(`完成：后台现有 ${refreshed.length} 款产品，本轮处理 ${imported.length} 款。历史迁移产品保持草稿，原有发布状态未改变。`);
}

main().catch((error) => {
  console.error(`迁移失败：${error.stack || error.message}`);
  process.exitCode = 1;
});
