"use client";

import dynamic from "next/dynamic";
import type { CSSProperties, MouseEventHandler, ReactNode } from "react";
import { useEnhancedEffects } from "./effects-provider";

const OpticalSurface = dynamic(() => import("./optical-surface"));

type GlassVariant = "nav" | "hero" | "card" | "control";
type GlassProps = { children: ReactNode; className?: string; id?: string; style?: CSSProperties; variant?: GlassVariant; interactive?: boolean; boosted?: boolean; liquid?: boolean; onClick?: MouseEventHandler<HTMLDivElement> };

const opticalOptions = {
  nav: { displacement: 14, blur: 16, saturate: 1.45 },
  hero: { displacement: 22, blur: 14, saturate: 1.55 },
  card: { displacement: 18, blur: 14, saturate: 1.5 },
  control: { displacement: 12, blur: 10, saturate: 1.4 },
} as const;

export function Glass({ children, className = "", id, style, variant = "card", interactive = false, boosted = false, liquid = false, onClick }: GlassProps) {
  const opticalEnabled = useEnhancedEffects() && liquid;
  const base = opticalOptions[variant];
  const classes = [
    "glass",
    `glass-${variant}`,
    liquid ? "glass-optical" : "glass-surface",
    interactive ? "glass-interactive" : "",
    boosted ? "is-boosted" : "",
    className,
  ].filter(Boolean).join(" ");

  if (opticalEnabled) return <OpticalSurface options={{ ...base, displacement: boosted ? base.displacement + 4 : base.displacement }} id={id} className={classes} style={style} onClick={onClick}>{children}</OpticalSurface>;
  return <div id={id} className={classes} style={style} onClick={onClick}>{children}</div>;
}
