"use client";

import { ArrowLeft, ArrowRight } from "lucide-react";
import { useEffect, useState } from "react";
import { HeroSection, type HeroSlide } from "@/components/hero-section";
import { CompanyStory, Journal, ProductBento, SiteFooter } from "@/components/home-sections";
import { ProductDetail } from "@/components/product-detail";
import { RichTextContent } from "@/components/rich-text-content";
import { SiteHeader } from "@/components/site-header";
import { api, type Article, type ContentPage, type Product, type SiteContent } from "@/lib/site-api";

const defaultSite = { name: "懒一夕智能科技", heroEyebrow: "杭州软件产品团队", heroTitle: "让复杂留在系统里｜把效率还给每一个人", heroDescription: "为创作、直播与高效工作打造更自然的软件工具，让技术融入工作，而不是打断工作。", heroLocation: "杭州余杭 · 2020 至今", productsIntro: "围绕真实工作流持续迭代。" } as SiteContent;
const defaultProduct: Product = { id: 0, slug: "writing-world", name: "写作天下", tagline: "AI 写作辅助软件", summary: "把灵感、素材、章节和智能辅助放进同一条工作流。", previewProject: "北境来信 · 长篇项目", previewChapter: "没有寄出的信", previewExcerpt: "雨是在傍晚停的。积水沿着屋檐落下，像一段迟迟不肯结束的省略号。", previewInsight: "这一段与前文的旧邮戳形成呼应，可以继续保持克制。" };

function ArticleView({ article, site, products }: { article: Article; site: SiteContent; products: Product[] }) {
  return <><main className="article-page"><a href="/news" className="back-link"><ArrowLeft size={16} /> 返回全部动态</a><header><span>{article.category}</span><h1>{article.title}</h1><p>{article.excerpt}</p><small>{String(article.publishedAt || article.updatedAt || "").slice(0, 10)}</small></header><article>{(article.content || "内容正在整理中。").split("\n").map((text, index) => <p key={index}>{text}</p>)}</article></main><SiteFooter site={site} products={products} /></>;
}

function NewsDirectory({ articles, site, products }: { articles: Article[]; site: SiteContent; products: Product[] }) {
  return <><main className="news-page"><a href="/" className="back-link"><ArrowLeft size={16} /> 返回首页</a><header><span>JOURNAL</span><h1>产品如何生长，<br />我们如何判断。</h1></header><div>{articles.map(article => <a href={`/news/${article.slug}`} key={article.id}><span>{article.category}</span><h2>{article.title}</h2><p>{article.excerpt}</p><ArrowRight size={20} /></a>)}</div></main><SiteFooter site={site} products={products} /></>;
}

function PageView({ page, site, products }: { page: ContentPage; site: SiteContent; products: Product[] }) {
  return <><main className="content-page"><a href="/" className="back-link"><ArrowLeft size={16}/> 返回首页</a><header><span>LYIXI / {page.slug.toUpperCase()}</span><h1>{page.title}</h1><p>{page.excerpt}</p></header>{page.coverUrl && <figure><img src={page.coverUrl} alt={page.title}/></figure>}<RichTextContent content={page.content || page.excerpt}/></main><SiteFooter site={site} products={products}/></>;
}

export default function Home() {
  const [site, setSite] = useState(defaultSite);
  const [products, setProducts] = useState<Product[]>([defaultProduct]);
  const [articles, setArticles] = useState<Article[]>([]);
  const [pages, setPages] = useState<ContentPage[]>([]);
  const [ready, setReady] = useState(false);
  const [pathname, setPathname] = useState("");
  const path = pathname.split("/").filter(Boolean);
  useEffect(() => {
    let active = true;
    const route = location.pathname.split("/").filter(Boolean);
    setPathname(location.pathname);
    setProducts([]);
    // Optional site settings must not block the current page.
    api<SiteContent>("/api/site/v1/site").then(data => { if (active) setSite({ ...defaultSite, ...data }); }).catch(() => {});
    async function loadPage() {
      if (route[0] === "product" && route[1]) {
        const product = await api<Product>(`/api/site/v1/products/${encodeURIComponent(route[1])}`);
        if (active) setProducts([product]);
      } else if (route[0] === "news") {
        const data = route[1]
          ? [await api<Article>(`/api/site/v1/articles/${encodeURIComponent(route[1])}`)]
          : await api<Article[]>("/api/site/v1/articles");
        if (active) setArticles(data);
      } else if (route.length && route[0] !== "product") {
        const page = await api<ContentPage>(`/api/site/v1/pages/${encodeURIComponent(route[0] === "page" ? route[1] : route[0])}`);
        if (active) setPages([page]);
      } else {
        const data = await api<Product[]>("/api/site/v1/products");
        if (active) setProducts(data);
      }
      if (active) setReady(true);
      if (route.length && (route[0] !== "product" || route[1])) {
        api<Product[]>("/api/site/v1/products").then(data => {
          if (active) setProducts(current => [...current, ...data.filter(item => !current.some(existing => existing.id === item.id))]);
        }).catch(() => {});
      }
    }
    if (!route.length) api<Article[]>("/api/site/v1/articles").then(data => { if (active) setArticles(data); }).catch(() => {});
    loadPage().catch(() => { /* Keep the server content visible when required data fails. */ });
    return () => { active = false; };
  }, []);
  useEffect(() => {
    if (ready) document.documentElement.setAttribute("data-site-ready", "true");
    return () => document.documentElement.removeAttribute("data-site-ready");
  }, [ready]);
  const selectedProduct = path[0] === "product" && path[1] ? products.find(item => item.slug === path[1]) : undefined;
  const selectedArticle = path[0] === "news" && path[1] ? articles.find(item => item.slug === path[1]) : undefined;
  const selectedPage = pages.find(item => item.slug === (path[0] === "page" ? path[1] : path[0]));
  if (!ready && path.length) return <div className="route-loading">正在打开内容…</div>;
  if (selectedProduct) return <><SiteHeader name={site.name} /><ProductDetail product={selectedProduct} site={site} products={products} /></>;
  if (selectedArticle) return <><SiteHeader name={site.name} /><ArticleView article={selectedArticle} site={site} products={products} /></>;
  if (selectedPage) return <><SiteHeader name={site.name}/><PageView page={selectedPage} site={site} products={products}/></>;
  if (path[0] === "product") return <><SiteHeader name={site.name}/><main><ProductBento products={products} site={site}/></main><SiteFooter site={site} products={products}/></>;
  if (path[0] === "news") return <><SiteHeader name={site.name} /><NewsDirectory articles={articles} site={site} products={products} /></>;
  let heroSlides: HeroSlide[] = [];
  try {
    const raw = JSON.parse(site.heroCarouselJson || "[]");
    if (Array.isArray(raw)) heroSlides = raw.map((slide: HeroSlide & { productSlug?: string; articleSlug?: string }) => {
      if (slide.type === "product" && slide.productSlug) { const product = products.find(item => item.slug === slide.productSlug); if (product) return { ...slide, title: slide.title || product.name, description: slide.description || product.tagline, imageUrl: slide.imageUrl || product.coverUrl, href: slide.href || `/product/${product.slug}` }; }
      if (slide.type === "article" && slide.articleSlug) { const article = articles.find(item => item.slug === slide.articleSlug); if (article) return { ...slide, title: slide.title || article.title, description: slide.description || article.excerpt, imageUrl: slide.imageUrl || article.coverUrl, href: slide.href || `/news/${article.slug}` }; }
      return slide;
    }).filter((slide: HeroSlide) => slide.title);
  } catch { heroSlides = []; }
  return <><SiteHeader name={site.name} /><main><HeroSection content={site} slides={heroSlides} /><ProductBento products={products} site={site} /><CompanyStory site={site} /><Journal articles={articles} site={site} /></main><SiteFooter site={site} products={products} /></>;
}
