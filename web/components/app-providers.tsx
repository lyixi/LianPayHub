"use client";

import { ThemeProvider } from "next-themes";
import { CustomCursor } from "./custom-cursor";
import { SiteBackground } from "./site-background";

export function AppProviders({ children }: { children: React.ReactNode }) {
  return <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
    <div className="glass-root"><SiteBackground />{children}<CustomCursor /></div>
  </ThemeProvider>;
}
