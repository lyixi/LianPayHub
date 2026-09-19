"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";

export function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  if (!mounted) return <button className="icon-button" aria-label="切换主题"><Sun size={17} /></button>;
  const dark = resolvedTheme === "dark";
  const toggle = () => {
    setTheme(dark ? "light" : "dark");
    window.setTimeout(() => window.dispatchEvent(new Event("resize")), 80);
  };
  return <button className="icon-button" aria-label={dark ? "切换到浅色主题" : "切换到深色主题"} onClick={toggle}>{dark ? <Sun size={17} /> : <Moon size={17} />}</button>;
}
