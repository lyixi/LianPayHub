"use client";

import { useLiquidGlass } from "@glinui/ui";
import type { HTMLAttributes } from "react";

type OpticalSurfaceProps = HTMLAttributes<HTMLDivElement> & {
  options: { displacement: number; blur: number; saturate: number };
};

export default function OpticalSurface({ options, children, className, style, ...props }: OpticalSurfaceProps) {
  const { ref, svgFilter, style: opticalStyle, isSupported } = useLiquidGlass({ ...options, profile: "squircle" });
  return <>{svgFilter}<div {...props} ref={ref} className={`${className}${isSupported ? " is-refracting" : ""}`} style={{ ...style, ...opticalStyle }}>{children}</div></>;
}
