"use client";

import { ArrowLeft, ArrowRight } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { HeroSection } from "@/components/hero-section";
import { Journal, Metrics, ProductBento, SiteCta, SiteFooter, Testimonial, TrustedRail } from "@/components/home-sections";
import { ProductDetail } from "@/components/product-detail";
import { SiteHeader } from "@/components/site-header";
import { api, type Article, type Product, type SiteContent } from "@/lib/site-api";

const defaultSite = { name: "懒一夕", heroTitle: "为创作者打造的工具", heroDescription: "写作天下、神奇弹幕——为创作者和高效工作者打造的产品。轻量、快速，不打断思路。", heroLocation: "杭州", productsIntro: "围绕真实工作流持续迭代。" } as SiteContent;
const defaultProduct: Product = { id: 0, slug: "writing-world", name: "写作天下", tagline: "AI 写作辅助软件", summary: "把灵感、素材、章节和智能辅助放进同一条工作流。", previewProject: "北境来信 · 长篇项目", previewChapter: "没有寄出的信", previewExcerpt: "雨是在傍晚停的。积水沿着屋檐落下，像一段迟迟不肯结束的省略号。", previewInsight: "这一段与前文的旧邮戳形成呼应，可以继续保持克制。" };

function ArticleView({ article, site, products }: { article: Article; site: SiteContent; products: Product[] }) {
  return <><main className="article-page"><a href="/news" className="back-link"><ArrowLeft size={16} /> 返回全部动态</a><header><span>{article.category}</span><h1>{article.title}</h1><p>{article.excerpt}</p><small>{String(article.publishedAt || article.updatedAt || "").slice(0, 10)}</small></header><article>{(article.content || "内容正在整理中。").split("\n").map((text, index) => <p key={index}>{text}</p>)}</article></main><SiteFooter site={site} products={products} /></>;
}

function NewsDirectory({ articles, site, products }: { articles: Article[]; site: SiteContent; products: Product[] }) {
  return <><main className="news-page"><a href="/" className="back-link"><ArrowLeft size={16} /> 返回首页</a><header><span>JOURNAL</span><h1>产品如何生长，<br />我们如何判断。</h1></header><div>{articles.map(article => <a href={`/news/${article.slug}`} key={article.id}><span>{article.category}</span><h2>{article.title}</h2><p>{article.excerpt}</p><ArrowRight size={20} /></a>)}</div></main><SiteFooter site={site} products={products} /></>;
}

export default function Home() {
  const [site, setSite] = useState(defaultSite);
  const [products, setProducts] = useState<Product[]>([defaultProduct]);
  const [articles, setArticles] = useState<Article[]>([]);
  const [ready, setReady] = useState(false);
  const [pathname, setPathname] = useState("");
  const path = pathname.split("/").filter(Boolean);
  useEffect(() => { setPathname(location.pathname); Promise.all([api<SiteContent>("/api/site/v1/site"), api<Product[]>("/api/site/v1/products"), api<Article[]>("/api/site/v1/articles")]).then(([siteData, productData, articleData]) => { setSite({ ...defaultSite, ...siteData }); setProducts(productData.length ? productData : [defaultProduct]); setArticles(articleData); }).finally(() => setReady(true)); }, []);
  const featured = useMemo(() => products.find(item => item.slug === site.featuredProductSlug) || products[0] || defaultProduct, [products, site.featuredProductSlug]);
  const selectedProduct = path[0] === "product" && path[1] ? products.find(item => item.slug === path[1]) : undefined;
  const selectedArticle = path[0] === "news" && path[1] ? articles.find(item => item.slug === path[1]) : undefined;
  if (!ready && path.length) return <div className="route-loading">正在打开内容…</div>;
  if (selectedProduct) return <><SiteHeader name={site.name} /><ProductDetail product={selectedProduct} site={site} products={products} /></>;
  if (selectedArticle) return <><SiteHeader name={site.name} /><ArticleView article={selectedArticle} site={site} products={products} /></>;
  if (path[0] === "news") return <><SiteHeader name={site.name} /><NewsDirectory articles={articles} site={site} products={products} /></>;
  return <><SiteHeader name={site.name} /><main><HeroSection content={site} product={featured} /><TrustedRail /><ProductBento products={products} site={site} /><Metrics site={site} /><Journal articles={articles} /><Testimonial /><SiteCta site={site} /></main><SiteFooter site={site} products={products} /></>;
}
