"use client";

import { useEffect, useState } from "react";
import { Menu, X } from "lucide-react";
import { Glass } from "./glass";
import { ThemeToggle } from "./theme-toggle";

export function SiteHeader({ name }: { name: string }) {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  useEffect(() => { const update = () => setScrolled(scrollY > 80); update(); addEventListener("scroll", update, { passive: true }); return () => removeEventListener("scroll", update); }, []);
  return <header className={`site-header ${scrolled ? "is-scrolled" : ""}`}>
    <Glass variant="nav" boosted={scrolled} className="nav-glass">
      <a className="brand" href="/" aria-label={`${name}首页`}><span className="brand-mark"><img src="/brand/favicon.png" alt="" /></span><span>{name}</span></a>
      <nav aria-label="主导航"><a href="/#products">产品</a><a href="/about">公司</a><a href="/news">动态</a><a href="/#contact">联系</a></nav>
      <div className="nav-actions"><ThemeToggle /><button type="button" className="mobile-nav-toggle" aria-label={menuOpen ? "关闭导航菜单" : "打开导航菜单"} aria-expanded={menuOpen} aria-controls="mobile-site-nav" onClick={() => setMenuOpen(value => !value)}>{menuOpen ? <X size={19} /> : <Menu size={19} />}</button></div>
    </Glass>
    {menuOpen && <nav id="mobile-site-nav" className="mobile-site-nav" aria-label="移动端主导航">{[["产品", "/#products"], ["公司", "/about"], ["动态", "/news"], ["联系", "/#contact"]].map(([label, href]) => <a href={href} key={href} onClick={() => setMenuOpen(false)}>{label}</a>)}</nav>}
  </header>;
}
