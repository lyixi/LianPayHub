const api = async (path, options = {}) => {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  const response = await fetch(path, { ...options, headers });
  const body = await response.json();
  if (body.code !== 0) throw new Error(body.message || "请求失败");
  return body.data;
};

const escapeHtml = value => String(value || "").replace(/[&<>"']/g, char => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
}[char]));
const colorAlpha = (hex, alpha) => /^#[0-9a-f]{6}$/i.test(hex || "") ? `${hex}${alpha}` : "#dfe5e2";
const normalizeAccent = (hex, index = 0) => {
  if (!/^#[0-9a-f]{6}$/i.test(hex || "")) return index % 2 ? "#c8a96e" : "#9a7b3f";
  const value = hex.toLowerCase();
  return ["#9a7b3f", "#c8a96e", "#d2ab65"].includes(value)
    ? hex
    : (index % 2 ? "#c8a96e" : "#9a7b3f");
};

function productRows(products) {
  return products.length ? products.map((product, index) => {
    const accent = normalizeAccent(product.accentColor, index);
    return `<a class="product-row reveal" href="/product/${encodeURIComponent(product.slug)}">
      <span class="product-no">${String(index + 1).padStart(2, "0")}</span>
      <div class="product-name"><h3>${escapeHtml(product.name)}</h3><span>${escapeHtml(product.tagline)}</span></div>
      <div class="product-visual" style="--accent:${accent};--accent-soft:${colorAlpha(accent, "1f")}"><div class="mini-window"></div></div>
      <p class="product-desc">${escapeHtml(product.summary || product.content || "持续打磨中的软件产品。")}</p>
      <span class="product-arrow">↗</span>
    </a>`;
  }).join("") : `<div class="empty-state">产品资料正在整理中。</div>`;
}

function renderProductRows(products) {
  document.querySelector("#productsGrid").innerHTML = productRows(products);
}

function journalCards(articles, limit) {
  const rows = typeof limit === "number" ? articles.slice(0, limit) : articles;
  return rows.length ? rows.map(article => `<a class="journal-card reveal" href="/news/${encodeURIComponent(article.slug)}">
    <small>${escapeHtml(article.category)} · ${String(article.publishedAt || "").slice(0, 10)}</small>
    <h3>${escapeHtml(article.title)}</h3>
    <p>${escapeHtml(article.excerpt || "查看最新产品进展。")}</p>
    <span>READ JOURNAL ↗</span>
  </a>`).join("") : `<div class="empty-state">产品动态正在准备中。</div>`;
}

function renderJournal(articles) {
  document.querySelector("#storiesList").innerHTML = journalCards(articles, 3);
}

function applyTextBindings(attribute, content) {
  document.querySelectorAll(`[${attribute}]`).forEach(node => {
    const key = node.getAttribute(attribute);
    if (content && content[key] !== null && content[key] !== undefined) node.textContent = content[key];
  });
}

function hydrateSite(site) {
  applyTextBindings("data-site", site);
  if (site?.name) document.title = `${site.name}｜让复杂留在系统里`;
  const email = document.querySelector("[data-site-email]");
  if (email && site?.email !== null && site?.email !== undefined) {
    email.textContent = site.email ? `${site.email} ↗` : "";
    email.href = site.email ? `mailto:${site.email}` : "#contact";
  }
}

function hydrateFeaturedProduct(product) {
  if (!product) return;
  applyTextBindings("data-featured", product);
  const stage = document.querySelector("#productStage");
  if (stage) {
    stage.setAttribute("aria-label", `${product.name}产品界面预览`);
    if (/^#[0-9a-f]{6}$/i.test(product.accentColor || "")) stage.style.setProperty("--featured-accent", product.accentColor);
  }
  const label = document.querySelector("[data-featured-label]");
  if (label) label.textContent = `PRODUCT / ${product.name}`;
  const summary = document.querySelector("[data-featured-summary]");
  if (summary) summary.textContent = product.summary || product.tagline || "";
  const link = document.querySelector("[data-featured-link]");
  if (link) {
    link.href = `/product/${encodeURIComponent(product.slug)}`;
    link.textContent = `探索${product.name} →`;
  }
  const chapters = [
    [product.previewChapter, product.previewExcerpt],
    [product.previewChapterTwo, product.previewExcerptTwo],
    [product.previewChapterThree, product.previewExcerptThree]
  ];
  document.querySelectorAll("[data-chapter]").forEach((button, index) => {
    const title = chapters[index]?.[0];
    const excerpt = chapters[index]?.[1];
    if (title !== null && title !== undefined) button.querySelector("b").textContent = title;
    button.dataset.previewTitle = title || button.querySelector("b")?.textContent || "";
    button.dataset.previewExcerpt = excerpt || "";
  });
  document.dispatchEvent(new CustomEvent("featured-product:hydrated", { detail: product }));
}

function renderDetail(item, isProduct) {
  document.title = `${item.name || item.title}｜懒一夕智能科技`;
  const name = escapeHtml(item.name || item.title);
  const description = escapeHtml(item.tagline || item.excerpt || item.summary || "");
  const content = escapeHtml(item.content || "更多内容正在整理中。").replace(/\n/g, "<br>");
  const chapters = isProduct ? [
    [item.previewChapter, item.previewExcerpt],
    [item.previewChapterTwo, item.previewExcerptTwo],
    [item.previewChapterThree, item.previewExcerptThree]
  ].filter(row => row[0] || row[1]) : [];
  const accent = normalizeAccent(item.accentColor);
  document.querySelector("main").innerHTML = `<article class="detail-page">
    <header class="detail-hero">
      <a class="detail-back" href="/${isProduct ? "product" : "news"}">← 返回${isProduct ? "产品目录" : "全部动态"}</a>
      <span>${isProduct ? "PRODUCT" : escapeHtml(item.category || "JOURNAL")}</span>
      <h1>${name}</h1><p>${description}</p>
      ${isProduct ? `<div class="detail-actions">
        ${item.downloadUrl ? `<a class="button button-primary" href="${escapeHtml(item.downloadUrl)}">下载产品 <span>↓</span></a>` : ""}
        ${item.purchaseUrl ? `<a class="button button-quiet" href="${escapeHtml(item.purchaseUrl)}">购买与授权 <span>↗</span></a>` : ""}
      </div>` : ""}
    </header>
    ${isProduct ? `<section class="detail-showcase" style="--detail-accent:${accent}">
      <div class="showcase-label">PRODUCT / ${escapeHtml(item.slug || "SYSTEM")}</div>
      <div class="showcase-frame"><div class="showcase-glow"></div><div class="showcase-window"><div class="showcase-bar"><i></i><i></i><i></i><span>${escapeHtml(item.name || "产品工作台")}</span></div><div class="showcase-body"><aside>${escapeHtml(item.previewProject || "工作台")}</aside><div><small>${escapeHtml(item.previewVolume || "PRODUCT WORKSPACE")}</small><h2>${escapeHtml(item.previewChapter || item.name || "专注工作")}</h2><p>${escapeHtml(item.previewBody || item.summary || "把复杂流程收束成一条顺畅的工作路径。")}</p><div class="showcase-line"></div></div><aside class="showcase-insight"><small>INSIGHT</small><p>${escapeHtml(item.previewInsight || item.tagline || "清晰、快速、可持续。")}</p></aside></div></div></div>
    </section>
    <section class="detail-sections"><div class="detail-section-intro"><span>01 / CAPABILITIES</span><h2>围绕真实工作，<br>把每一步做得更顺。</h2><p>${escapeHtml(item.summary || item.tagline || "")}</p></div><div class="detail-feature-grid">${chapters.map((row, index) => `<article><small>0${index + 1}</small><h3>${escapeHtml(row[0] || "工作模块")}</h3><p>${escapeHtml(row[1] || item.previewBody || item.content || "持续打磨的产品能力。")}</p></article>`).join("") || `<article><small>01</small><h3>稳定的产品体验</h3><p>${content}</p></article>`}</div></section>
    <section class="detail-content-band"><div class="detail-number">02 / PRODUCT NOTE</div><div class="detail-content"><p>${content}</p></div><aside><small>LYIXI PRODUCT STUDIO</small><p>持续维护、可靠交付的软件产品。</p></aside></section>
    <section class="detail-cta"><span>READY WHEN YOU ARE</span><h2>让下一次工作，<br>从更顺开始。</h2><a class="button button-primary" href="#detail-contact">开始使用 <span>↗</span></a></section>` : `<section class="detail-workbench"><div class="detail-number">${String(item.publishedAt || "").slice(0, 10)}</div><div class="detail-content"><p>${content}</p></div><aside><small>懒一夕智能科技</small><p>记录产品进展、设计判断与工程实践。</p></aside></section>`}
  </article>`;
}

function renderDirectory(items, isProduct) {
  document.title = `${isProduct ? "产品目录" : "产品动态"}｜懒一夕智能科技`;
  const list = isProduct ? `<div class="directory-products">${productRows(items)}</div>` :
    `<div class="directory-journal">${journalCards(items)}</div>`;
  document.querySelector("main").innerHTML = `<article class="directory-page">
    <header class="directory-hero">
      <a class="detail-back" href="/">← 返回首页</a>
      <span>${isProduct ? "PRODUCT DIRECTORY" : "LYIXI JOURNAL"}</span>
      <h1>${isProduct ? "产品不是功能集合，\n是完整的工作方式。" : "记录产品如何生长，\n也记录我们如何判断。"}</h1>
      <p>${isProduct ? "围绕真实场景长期迭代，共享稳定的账户、支付、同步与智能能力。" : "产品进展、设计取舍与工程实践，都在这里持续公开。"}</p>
    </header>
    <section class="directory-body"><div class="directory-label">${isProduct ? "ALL PRODUCTS" : "ALL STORIES"} / ${String(items.length).padStart(2, "0")}</div>${list}</section>
  </article>`;
}

function activateReveal() {
  const nodes = document.querySelectorAll(".reveal");
  if (matchMedia("(prefers-reduced-motion: reduce)").matches) return nodes.forEach(node => node.classList.add("visible"));
  const observer = new IntersectionObserver(entries => entries.forEach(entry => {
    if (entry.isIntersecting) { entry.target.classList.add("visible"); observer.unobserve(entry.target); }
  }), { threshold: .14 });
  nodes.forEach(node => observer.observe(node));
}

async function boot() {
  try {
    const parts = location.pathname.split("/").filter(Boolean);
    const site = await api("/api/site/v1/site");
    hydrateSite(site);
    if ((parts[0] === "product" || parts[0] === "news") && parts[1]) {
      const isProduct = parts[0] === "product";
      renderDetail(await api(`/api/site/v1/${isProduct ? "products" : "articles"}/${encodeURIComponent(parts[1])}`), isProduct);
    } else if (parts[0] === "product" || parts[0] === "news") {
      const isProduct = parts[0] === "product";
      renderDirectory(await api(`/api/site/v1/${isProduct ? "products" : "articles"}`), isProduct);
    } else {
      const [products, articles] = await Promise.all([api("/api/site/v1/products"), api("/api/site/v1/articles")]);
      renderProductRows(products);
      renderJournal(articles);
      hydrateFeaturedProduct(products.find(product => product.slug === site.featuredProductSlug) || products[0]);
    }
    activateReveal();
  } catch (error) {
    const target = document.querySelector("#productsGrid") || document.querySelector("main");
    if (target) target.innerHTML = `<div class="empty-state load-error">内容暂时无法加载，请稍后重试。</div>`;
    activateReveal();
  }
}

const leadForm = document.querySelector("#leadForm");
if (leadForm) leadForm.addEventListener("submit", async event => {
  event.preventDefault();
  const button = leadForm.querySelector("button");
  const message = document.querySelector("#formMessage");
  button.disabled = true;
  message.textContent = "正在发送…";
  try {
    await api("/api/site/v1/leads", { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(leadForm))) });
    leadForm.reset();
    message.textContent = "信息已收到，我们会尽快联系你。";
  } catch (error) {
    message.textContent = "发送失败，请稍后重试。";
  } finally {
    button.disabled = false;
  }
});

boot();
