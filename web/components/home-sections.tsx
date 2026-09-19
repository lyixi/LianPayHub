"use client";

import { ArrowRight, BookOpen, Download, MessageSquareText, Radio, Sparkles, Zap } from "lucide-react";
import { motion } from "framer-motion";
import { Glass } from "./glass";
import type { Article, Product, SiteContent } from "@/lib/site-api";

const reveal = { initial: { opacity: 0, y: 28 }, whileInView: { opacity: 1, y: 0 }, viewport: { once: true, margin: "-12%" }, transition: { duration: .65, ease: [.16, 1, .3, 1] as const } };

function ProductVisual({ product, compact = false }: { product: Product; compact?: boolean }) {
  const danmu = /弹幕|直播/.test(product.name);
  return <div className={`product-visual ${danmu ? "visual-live" : "visual-writing"} ${compact ? "is-compact" : ""}`}>
    {danmu ? <><div className="live-screen"><Radio size={20} /><span>LIVE</span><b>12,486</b></div><div className="danmu-lines"><i>欢迎来到直播间</i><i>关键词抽奖已开启</i><i>这套特效太流畅了</i></div></> : <><div className="document-lines"><i /><i /><i /><i /></div><div className="ai-suggestion"><Sparkles size={14} /><span>{product.previewInsight || "根据上下文续写，并保留你的表达方式。"}</span></div></>}
  </div>;
}

export function TrustedRail() {
  const names = ["创作者", "内容团队", "直播运营", "独立开发者", "品牌团队", "效率工作者"];
  return <section className="trusted"><span>BUILT FOR</span><div className="trusted-track">{[...names, ...names].map((name, index) => <Glass variant="control" className="trusted-item" key={`${name}-${index}`}><b>{name.slice(0, 1)}</b>{name}</Glass>)}</div></section>;
}

export function ProductBento({ products, site }: { products: Product[]; site: SiteContent }) {
  return <section className="products-section" id="products"><motion.header {...reveal}><span className="section-label">PRODUCTS / 产品</span><h2>{site.productsHeading || "让工具融入工作，而不是打断工作。"}</h2><p>{site.productsIntro || "每一款产品都来自真实工作流。产品数量、顺序和内容由后台管理，官网会自动同步最新版本。"}</p></motion.header><div className="product-catalog">
    {products.map((product, index) => index === 0 ? <motion.a href={`/product/${product.slug}`} className="product-feature" key={product.id} {...reveal}><Glass variant="hero" liquid interactive className="product-glass"><div className="product-copy"><span>{String(index + 1).padStart(2, "0")} / FEATURED</span><div><h3>{product.name}</h3><p>{product.summary || product.tagline}</p></div><b>查看产品 <ArrowRight size={17} /></b></div><ProductVisual product={product} /></Glass></motion.a> : <motion.a href={`/product/${product.slug}`} className="product-item" key={product.id} {...reveal}><Glass variant="card" interactive className="product-item-glass"><ProductVisual product={product} compact /><div className="product-item-copy"><span>{String(index + 1).padStart(2, "0")} / PRODUCT</span><h3>{product.name}</h3><p>{product.summary || product.tagline}</p><b>了解详情 <ArrowRight size={16} /></b></div></Glass></motion.a>)}
  </div></section>;
}

export function Metrics({ site }: { site: SiteContent }) {
  const data = [["500万+", "累计用户"], [site.yearsLabel || "9 年", site.yearsDescription || "持续构建工具"], [String(site.productCountLabel || "16+"), site.productCountDescription || "产品与服务"], ["99.99%", "核心服务可用性"]];
  return <section className="metrics">{data.map(([value, label]) => <motion.div key={label} {...reveal}><Glass variant="card" className="metric-glass"><strong>{value}</strong><span>{label}</span></Glass></motion.div>)}</section>;
}

export function Journal({ articles }: { articles: Article[] }) {
  return <section className="journal" id="journal"><motion.header {...reveal}><span className="section-label">JOURNAL / 动态</span><h2>产品持续生长，<br />我们如实记录。</h2><a href="/news">全部动态 <ArrowRight size={16} /></a></motion.header><div className="journal-grid">{articles.slice(0, 3).map((article, index) => <motion.a href={`/news/${article.slug}`} className="journal-card fallback-card" key={article.id} {...reveal}><span>{article.category} · {String(article.publishedAt || article.updatedAt || "").slice(0, 10)}</span><div><small>0{index + 1}</small><h3>{article.title}</h3><p>{article.excerpt}</p></div><ArrowRight size={19} /></motion.a>)}</div></section>;
}

export function Testimonial() {
  return <section className="testimonial"><motion.div {...reveal}><Glass variant="hero" className="quote-glass"><MessageSquareText size={24} /><blockquote>“它真正理解创作不是填满输入框，而是保护那条不能被打断的思路。”</blockquote><footer><b>一位长期用户</b><span>内容创作者 / 写作天下</span></footer></Glass></motion.div></section>;
}

export function SiteCta({ site }: { site: SiteContent }) {
  return <section className="site-cta" id="help"><motion.div {...reveal}><span className="section-label">START NOW</span><h2>{site.contactHeading || "把时间留给真正重要的事。"}</h2><p>{site.contactDescription || "选择适合你的产品，今天就开始更顺畅地工作。"}</p><Glass variant="control" interactive className="cta-button"><a href="#products">找到适合我的产品 <ArrowRight size={17} /></a></Glass></motion.div></section>;
}

export function SiteFooter({ site, products }: { site: SiteContent; products: Product[] }) {
  return <footer className="footer"><div className="footer-brand"><b>LY</b><h2>{site.name || "懒一夕"}</h2><p>让复杂留在系统里。</p></div><div><span>产品</span>{products.map(product => <a href={`/product/${product.slug}`} key={product.id}>{product.name}</a>)}</div><div><span>公司</span><a href="#journal">更新日志</a><a href="#help">帮助</a><a href="/admin/">运营后台</a></div><div><span>联系</span><a href={`mailto:${site.email || "wxy@iwxyi.com"}`}>{site.email || "wxy@iwxyi.com"}</a><a href="https://beian.miit.gov.cn">{site.icpNumber}</a></div><small>{site.footerCopyright || "© 2026 懒一夕智能科技"}</small></footer>;
}
