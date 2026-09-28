"use client";

import { useEffect, useState } from "react";
import { Glass } from "./glass";
import { ThemeToggle } from "./theme-toggle";

export function SiteHeader({ name }: { name: string }) {
  const [scrolled, setScrolled] = useState(false);
  useEffect(() => { const update = () => setScrolled(scrollY > 80); update(); addEventListener("scroll", update, { passive: true }); return () => removeEventListener("scroll", update); }, []);
  return <header className={`site-header ${scrolled ? "is-scrolled" : ""}`}>
    <Glass variant="nav" boosted={scrolled} className="nav-glass">
      <a className="brand" href="#top" aria-label={`${name}首页`}><span className="brand-mark"><img src="/brand/favicon.png" alt="" /></span><span>{name}</span></a>
      <nav aria-label="主导航"><a href="/#products">产品</a><a href="/about">公司</a><a href="/news">动态</a><a href="/#contact">联系</a></nav>
      <div className="nav-actions"><ThemeToggle /></div>
    </Glass>
  </header>;
}
