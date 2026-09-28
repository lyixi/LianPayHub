"use client";

import { ArrowRight, Image as ImageIcon, Radio, Sparkles } from "lucide-react";
import { motion } from "framer-motion";
import { Glass } from "./glass";
import type { Article, Product, SiteContent } from "@/lib/site-api";

const reveal = { initial: { opacity: 0, y: 28 }, whileInView: { opacity: 1, y: 0 }, viewport: { once: true, margin: "-12%" }, transition: { duration: .65, ease: [.16, 1, .3, 1] as const } };

function ProductVisual({ product, compact = false }: { product: Product; compact?: boolean }) {
  let media = product.coverUrl;
  if (!media) try { const parsed = JSON.parse(product.mediaJson || "[]"); media = Array.isArray(parsed) ? parsed.find(item => item?.url)?.url : undefined; } catch { media = undefined; }
  const danmu = /弹幕|直播/.test(product.name);
  return <div className={`product-visual ${media ? "has-product-media" : danmu ? "visual-live" : "visual-writing"} ${compact ? "is-compact" : ""}`}>
    {media ? <img src={media} alt={`${product.name} 产品封面`} /> : danmu ? <><div className="live-screen"><Radio size={20} /><span>LIVE</span><b>12,486</b></div><div className="danmu-lines"><i>欢迎来到直播间</i><i>关键词抽奖已开启</i><i>这套特效太流畅了</i></div><span className="product-media-note"><ImageIcon size={14}/> 等待产品图片</span></> : <><div className="document-lines"><i /><i /><i /><i /></div><div className="ai-suggestion"><Sparkles size={14} /><span>{product.previewInsight || "产品图片上传后将在这里自动展示。"}</span></div><span className="product-media-note"><ImageIcon size={14}/> 媒体预留位</span></>}
  </div>;
}

export function ProductBento({ products, site }: { products: Product[]; site: SiteContent }) {
  return <section className="products-section" id="products"><motion.header {...reveal}><span className="section-label">PRODUCTS / 产品</span><h2>{site.productsHeading || "让工具融入工作，而不是打断工作。"}</h2><p>{site.productsIntro || "每一款产品都来自真实工作流。产品数量、顺序和内容由后台管理，官网会自动同步最新版本。"}</p></motion.header><div className="product-catalog">
    {products.map((product, index) => index === 0 ? <motion.a href={`/product/${product.slug}`} className="product-feature" key={product.id} {...reveal}><Glass variant="hero" liquid interactive className="product-glass"><div className="product-copy"><span>精选产品</span><div><h3>{product.name}</h3><p>{product.summary || product.tagline}</p></div><b>查看详情 <ArrowRight size={17} /></b></div><ProductVisual product={product} /></Glass></motion.a> : <motion.a href={`/product/${product.slug}`} className="product-item" key={product.id} {...reveal}><Glass variant="card" interactive className="product-item-glass"><ProductVisual product={product} compact /><div className="product-item-copy"><h3>{product.name}</h3><p>{product.summary || product.tagline}</p><b>查看详情 <ArrowRight size={16} /></b></div></Glass></motion.a>)}
  </div></section>;
}

export function CompanyStory({ site }: { site: SiteContent }) {
  let principles: { title: string; description?: string }[];
  try { const parsed = JSON.parse(site.companyPrinciplesJson || ""); principles = Array.isArray(parsed) ? parsed.filter(item => item?.title) : []; }
  catch { principles = [[site.principleOneTitle, site.principleOneDescription],[site.principleTwoTitle, site.principleTwoDescription],[site.principleThreeTitle, site.principleThreeDescription]].filter(item => item[0]).map(([title, description]) => ({ title: title || "", description })); }
  return <section className={`company-story ${principles.length ? "" : "without-principles"}`} id="company"><motion.div className="company-statement" {...reveal}><span className="section-label">ABOUT / 公司</span><h2>{site.companyHeading || "我们相信，真正好的工具会逐渐隐身。"}</h2><p>{site.companyDescription || "懒一夕是一家持续构建效率软件的科技公司。我们关注创作、直播与日常工作中那些反复消耗注意力的问题，并用产品把复杂留在系统里。"}</p><div className="company-links"><a href="/about">了解公司 <ArrowRight size={16}/></a></div></motion.div>{principles.length ? <div className="company-principles">{principles.map((item, index) => <motion.article key={`${item.title}-${index}`} {...reveal}><h3>{item.title}</h3>{item.description && <p>{item.description}</p>}</motion.article>)}</div> : null}</section>;
}

export function Journal({ articles, site }: { articles: Article[]; site: SiteContent }) {
  return <section className="journal" id="journal"><motion.header {...reveal}><span className="section-label">JOURNAL / 动态</span><h2>{site.journalHeading || <>产品持续生长，<br />我们如实记录。</>}</h2>{articles.length > 3 && <a href="/news">全部动态 <ArrowRight size={16} /></a>}</motion.header>{articles.length ? <div className="journal-grid">{articles.slice(0, 3).map((article) => <motion.a href={`/news/${article.slug}`} className="journal-card fallback-card" key={article.id} {...reveal}><span>{article.category} · {String(article.publishedAt || article.updatedAt || "").slice(0, 10)}</span><div><h3>{article.title}</h3><p>{article.excerpt}</p></div><ArrowRight size={19} /></motion.a>)}</div> : null}</section>;
}

export function SiteFooter({ site, products }: { site: SiteContent; products: Product[] }) {
  return <footer className="footer" id="contact"><div className="footer-brand"><b><img src="/brand/favicon.png" alt="" /></b><h2>{site.name || "懒一夕"}</h2><p>{site.contactHeading || "让复杂留在系统里。"}</p></div><div><span>产品</span>{products.slice(0, 5).map(product => <a href={`/product/${product.slug}`} key={product.id}>{product.name}</a>)}{products.length > 5 && <a href="/#products">查看全部产品</a>}</div><div><span>公司</span><a href="/about">关于我们</a><a href="/culture">企业文化</a><a href="/news">新闻动态</a></div><div><span>联系</span><a href={`mailto:${site.email || "wxy@iwxyi.com"}`}>{site.email || "wxy@iwxyi.com"}</a>{site.icpNumber && <a href="https://beian.miit.gov.cn">{site.icpNumber}</a>}</div><small>{site.footerCopyright || "© 2026 懒一夕智能科技"}</small></footer>;
}
