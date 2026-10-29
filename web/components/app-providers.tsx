"use client";

import { ThemeProvider } from "next-themes";
import { SiteBackground } from "./site-background";
import { EffectsProvider } from "./effects-provider";

export function AppProviders({ children }: { children: React.ReactNode }) {
  return <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
    <EffectsProvider><SiteBackground />{children}</EffectsProvider>
  </ThemeProvider>;
}
