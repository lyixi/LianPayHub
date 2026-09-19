"use client";

import { ArrowDown, ArrowUpRight, Sparkles } from "lucide-react";
import { motion } from "framer-motion";
import { Glass } from "./glass";

export type HeroContent = { name: string; heroTitle: string; heroDescription: string; heroLocation: string };
export type FeaturedProduct = { name: string; tagline: string; previewProject?: string; previewChapter?: string; previewExcerpt?: string; previewInsight?: string };

const line = { hidden: { opacity: 0, y: 44 }, show: { opacity: 1, y: 0 } };

function HeroPreview({ product }: { product: FeaturedProduct }) {
  return <div className="hero-liquid-stage">
    <div className="hero-prism hero-prism-a" aria-hidden="true" />
    <div className="hero-prism hero-prism-b" aria-hidden="true" />
    <Glass variant="hero" liquid className="hero-preview">
    <div className="preview-top"><span><Sparkles size={14} /> {product.name || "写作天下"}</span><b>AI READY</b></div>
    <div className="preview-workspace"><aside><strong>写</strong><i className="active" /><i /><i /><i /></aside><div className="preview-tree"><small>当前项目</small><b>{product.previewProject || "北境来信 · 长篇项目"}</b><span>01 人物与世界</span><span className="selected">02 {product.previewChapter || "没有寄出的信"}</span><span>03 旧站台</span></div><article><small>AUTOSAVED · 2,416 字</small><h2>{product.previewChapter || "没有寄出的信"}</h2><p>{product.previewExcerpt || "雨是在傍晚停的。积水沿着屋檐落下，像一段迟迟不肯结束的省略号。"}</p><div className="writing-caret" /></article></div>
    <div className="preview-insight"><span>智能提示</span><p>{product.previewInsight || "这一段与前文的旧邮戳形成呼应，可以继续保持克制。"}</p></div>
    </Glass>
    <div className="refraction-status" aria-hidden="true"><i /> SVG REFRACTION</div>
  </div>;
}

export function HeroSection({ content, product }: { content: HeroContent; product: FeaturedProduct }) {
  return <section className="hero" id="top">
    <div className="hero-copy">
      <motion.div className="hero-meta" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: .1 }}><span>EST. 2017</span><i /> <span>效率工具</span><i /> <span>{content.heroLocation || "杭州"}</span></motion.div>
      <motion.h1 initial="hidden" animate="show" transition={{ staggerChildren: .09, delayChildren: .12 }}>
        <motion.span variants={line}>为创作者</motion.span><motion.span variants={line}>打造的</motion.span><motion.span variants={line}>工具<span className="accent-dot">。</span></motion.span>
      </motion.h1>
      <motion.p initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: .48, duration: .6 }}>{content.heroDescription || "写作天下、神奇弹幕，两款为真实工作而生的产品。轻量、快速，不打断思路。"}</motion.p>
      <motion.div className="hero-actions" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: .58 }}>
        <Glass variant="control" interactive className="hero-button hero-button-primary"><a href="#products">探索产品 <ArrowDown size={16} /></a></Glass>
        <Glass variant="control" interactive className="hero-button"><a href="#preview">观看产品现场 <ArrowUpRight size={16} /></a></Glass>
      </motion.div>
    </div>
    <motion.div id="preview" className="hero-preview-shell" initial={{ opacity: 0, scale: .94, y: 30 }} animate={{ opacity: 1, scale: 1, y: 0 }} transition={{ delay: .3, duration: .8, ease: [.16, 1, .3, 1] }}><HeroPreview product={product}/></motion.div>
    <div className="scroll-cue"><span>SCROLL</span><i /></div>
  </section>;
}
