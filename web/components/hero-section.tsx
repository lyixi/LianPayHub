"use client";

import { ArrowLeft, ArrowRight, ArrowDown, ArrowUpRight, Image as ImageIcon } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { Glass } from "./glass";

export type HeroContent = { name: string; heroTitle: string; heroDescription: string; heroLocation: string; heroEyebrow?: string };
export type HeroSlide = { type?: "product" | "article" | "custom"; display?: "card" | "banner" | "tag"; title: string; eyebrow?: string; description?: string; imageUrl?: string; href?: string };

const line = { hidden: { opacity: 0, y: 44 }, show: { opacity: 1, y: 0 } };

function titleLines(title: string) {
  const normalized = (title || "为创作者打造的工具").trim();
  const parts = normalized.split(/[｜|\n]/).filter(Boolean);
  if (parts.length > 1) return parts.slice(0, 3);
  const size = Math.ceil(normalized.length / 3);
  return [normalized.slice(0, size), normalized.slice(size, size * 2), normalized.slice(size * 2)].filter(Boolean);
}

function HeroPreview({ slides }: { slides: HeroSlide[] }) {
  const [active, setActive] = useState(0);
  const slide = slides[active];
  useEffect(() => { if (slides.length < 2) return; const timer = window.setInterval(() => setActive(value => (value + 1) % slides.length), 5200); return () => window.clearInterval(timer); }, [slides.length]);
  if (!slide) return null;
  const media = slide.imageUrl;
  return <div className="hero-liquid-stage">
    <div className="hero-prism hero-prism-a" aria-hidden="true" />
    <div className="hero-prism hero-prism-b" aria-hidden="true" />
    <Glass variant="hero" liquid className={`hero-preview hero-product-stage hero-slide-${slide.display || "card"}`}>
      <div className="hero-product-bar"><span>{slide.eyebrow || (slide.type === "article" ? "最新动态" : "首页精选")}</span>{slides.length > 1 && <span className="hero-slide-count">{String(active + 1).padStart(2, "0")} / {String(slides.length).padStart(2, "0")}</span>}</div>
      <div className={`hero-product-media ${media ? "has-media" : "is-placeholder"}`}>
        {media ? <img src={media} alt={slide.title} /> : <><div className="media-placeholder-mark">{slide.title.slice(0, 1)}</div><div><ImageIcon size={18}/><span>首页视觉预留位</span><small>在后台为这张幻灯片上传图片</small></div></>}
      </div>
      <div className="hero-product-caption"><div><span>{slide.description || "为访客优先展示重要内容。"}</span><h2>{slide.title}</h2></div>{slide.href && <a href={slide.href} aria-label={`查看${slide.title}`}><ArrowUpRight size={20}/></a>}</div>
      {slides.length > 1 && <div className="hero-slide-controls"><button type="button" onClick={() => setActive(value => (value - 1 + slides.length) % slides.length)} aria-label="上一张"><ArrowLeft size={14}/></button><div>{slides.map((item, index) => <button type="button" key={`${item.title}-${index}`} className={index === active ? "is-active" : ""} onClick={() => setActive(index)} aria-label={`第 ${index + 1} 张`} />)}</div><button type="button" onClick={() => setActive(value => (value + 1) % slides.length)} aria-label="下一张"><ArrowRight size={14}/></button></div>}
    </Glass>
  </div>;
}

export function HeroSection({ content, slides }: { content: HeroContent; slides?: HeroSlide[] }) {
  return <section className={`hero ${slides?.length ? "has-hero-slides" : "without-hero-slides"}`} id="top">
    <div className="hero-copy">
      <motion.h1 initial="hidden" animate="show" transition={{ staggerChildren: .09, delayChildren: .12 }}>
        {titleLines(content.heroTitle).map((part, index, lines) => <motion.span variants={line} key={`${part}-${index}`}>{part}{index === lines.length - 1 && <span className="accent-dot">。</span>}</motion.span>)}
      </motion.h1>
      <motion.p initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: .48, duration: .6 }}>{content.heroDescription || "写作天下、神奇弹幕，两款为真实工作而生的产品。轻量、快速，不打断思路。"}</motion.p>
      <motion.div className="hero-actions" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: .58 }}>
        <Glass variant="control" interactive className="hero-button hero-button-primary"><a href="#products">探索产品 <ArrowDown size={16} /></a></Glass>
        <Glass variant="control" interactive className="hero-button"><a href="#company">了解我们 <ArrowUpRight size={16} /></a></Glass>
      </motion.div>
    </div>
    {slides?.length ? <motion.div id="preview" className="hero-preview-shell" initial={{ opacity: 0, scale: .94, y: 30 }} animate={{ opacity: 1, scale: 1, y: 0 }} transition={{ delay: .3, duration: .8, ease: [.16, 1, .3, 1] }}><HeroPreview slides={slides}/></motion.div> : null}
  </section>;
}
