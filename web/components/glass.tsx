"use client";

import { useLiquidGlass } from "@glinui/ui";
import type { CSSProperties, MouseEventHandler, ReactNode } from "react";
import { useEffect, useState } from "react";

type GlassVariant = "nav" | "hero" | "card" | "control";
type GlassProps = { children: ReactNode; className?: string; id?: string; style?: CSSProperties; variant?: GlassVariant; interactive?: boolean; boosted?: boolean; liquid?: boolean; onClick?: MouseEventHandler<HTMLDivElement> };

const opticalOptions = {
  nav: { displacement: 14, blur: 16, saturate: 1.45 },
  hero: { displacement: 22, blur: 14, saturate: 1.55 },
  card: { displacement: 18, blur: 14, saturate: 1.5 },
  control: { displacement: 12, blur: 10, saturate: 1.4 },
} as const;

export function Glass({ children, className = "", id, style, variant = "card", interactive = false, boosted = false, liquid = false, onClick }: GlassProps) {
  const [opticalEnabled, setOpticalEnabled] = useState(false);
  useEffect(() => {
    const desktop = window.matchMedia("(min-width: 900px)");
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)");
    const update = () => setOpticalEnabled(liquid && desktop.matches && !reduced.matches);
    update();
    desktop.addEventListener("change", update);
    reduced.addEventListener("change", update);
    return () => {
      desktop.removeEventListener("change", update);
      reduced.removeEventListener("change", update);
    };
  }, [liquid]);
  const base = opticalOptions[variant];
  const { ref, svgFilter, style: opticalStyle, isSupported } = useLiquidGlass({
    ...base,
    displacement: boosted ? base.displacement + 4 : base.displacement,
    profile: "squircle",
    disabled: !opticalEnabled,
  });
  const classes = [
    "glass",
    `glass-${variant}`,
    liquid ? "glass-optical" : "glass-surface",
    liquid && isSupported ? "is-refracting" : "",
    interactive ? "glass-interactive" : "",
    boosted ? "is-boosted" : "",
    className,
  ].filter(Boolean).join(" ");

  return <>
    {svgFilter}
    <div ref={ref} id={id} className={classes} style={{ ...style, ...opticalStyle }} onClick={onClick}>{children}</div>
  </>;
}
