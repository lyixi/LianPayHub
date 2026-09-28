"use client";

import { ArrowLeft, ArrowRight, Download, Image as ImageIcon, Sparkles } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Glass } from "./glass";
import type { Product, ProductAsset, SiteContent } from "@/lib/site-api";
import { SiteFooter } from "./home-sections";
import { RichTextContent } from "./rich-text-content";

type DownloadAsset = { id: number; url: string; title?: string; fileName?: string; contentType?: string };

export function ProductDetail({ product, site, products }: { product: Product; site: SiteContent; products: Product[] }) {
  const assets = useMemo<ProductAsset[]>(() => { try { const parsed = JSON.parse(product.mediaJson || "[]"); return Array.isArray(parsed) ? parsed : []; } catch { return []; } }, [product.mediaJson]);
  const slides = assets.length ? assets : product.coverUrl ? [{ id: 0, url: product.coverUrl, title: product.name }] : [];
  const downloads = useMemo<DownloadAsset[]>(() => { try { const parsed = JSON.parse(product.downloadJson || "[]"); return Array.isArray(parsed) ? parsed : []; } catch { return []; } }, [product.downloadJson]);
  const [active, setActive] = useState(0);
  useEffect(() => setActive(0), [product.id]);
  const current = slides[active] || slides[0];
  const primaryDownload = downloads[0];

  return <><main className="product-page">
    <section className="product-hero"><a href="/#products" className="back-link"><ArrowLeft size={16} /> 返回产品列表</a><h1>{product.name}</h1><p>{product.tagline}</p>{primaryDownload ? <div className="detail-actions"><Glass variant="control" interactive><a href={primaryDownload.url} download={primaryDownload.fileName || true}>立即下载 <Download size={16} /></a></Glass></div> : product.downloadUrl ? <div className="detail-actions"><Glass variant="control" interactive><a href={product.downloadUrl}>立即下载 <Download size={16} /></a></Glass></div> : null}</section>
    <section className="detail-visual"><Glass variant="hero" liquid className="detail-visual-glass"><div className="detail-media-stage">{current ? <img src={current.url} alt={current.altText || current.title || product.name}/> : <div className="detail-app"><aside><b>{product.name.slice(0, 1)}</b><i /><i /><i /></aside><article><small>{product.name}</small><h2>{product.tagline}</h2><p>{product.summary}</p><div className="detail-line" /></article><div className="detail-assist"><Sparkles size={18} /><b>产品亮点</b><p>{product.summary}</p></div></div>}</div>{slides.length > 1 && <div className="detail-gallery"><button type="button" className="detail-gallery-arrow" onClick={() => setActive((active - 1 + slides.length) % slides.length)} aria-label="上一张"><ArrowLeft size={17}/></button><div className="detail-gallery-thumbs">{slides.map((slide, index) => <button type="button" className={index === active ? "is-active" : ""} key={`${slide.id}-${slide.url}`} onClick={() => setActive(index)}><img src={slide.url} alt=""/></button>)}</div><button type="button" className="detail-gallery-arrow" onClick={() => setActive((active + 1) % slides.length)} aria-label="下一张"><ArrowRight size={17}/></button></div>}{slides.length === 0 && <div className="detail-no-media"><ImageIcon size={16}/> 产品媒体即将上线</div>}</Glass></section>
    <section className="product-story"><header><span className="section-label">产品介绍</span>{product.summary && <p>{product.summary}</p>}</header><RichTextContent content={product.content || product.summary}/></section>
  </main><SiteFooter site={site} products={products} /></>;
}
