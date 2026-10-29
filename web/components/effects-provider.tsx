"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

const EnhancedEffects = createContext(false);

export function useEnhancedEffects() {
  return useContext(EnhancedEffects);
}

export function EffectsProvider({ children }: { children: ReactNode }) {
  const [enhanced, setEnhanced] = useState(false);

  useEffect(() => {
    const motion = matchMedia("(prefers-reduced-motion: reduce)");
    const desktop = matchMedia("(min-width: 900px)");
    const device = navigator as Navigator & { deviceMemory?: number; connection?: { saveData?: boolean } };
    let frame = 0;
    let previous = 0;
    let samples = 0;
    let slow = 0;
    let allowed = false;
    let downgraded = false;

    const sample = (now: number) => {
      frame = 0;
      if (document.hidden || !allowed) return;
      if (previous) {
        samples++;
        if (now - previous > 32) slow++;
      }
      previous = now;
      // Sample startup and scrolling, without keeping an idle animation loop alive.
      if (samples >= 45) {
        if (slow / samples > 0.25) {
          downgraded = true;
          allowed = false;
          setEnhanced(false);
        }
        return;
      }
      frame = requestAnimationFrame(sample);
    };
    const measure = () => {
      if (frame || !allowed || document.hidden) return;
      previous = samples = slow = 0;
      frame = requestAnimationFrame(sample);
    };
    const update = () => {
      allowed = !downgraded && desktop.matches && !motion.matches &&
        !(device.hardwareConcurrency > 0 && device.hardwareConcurrency <= 4) &&
        !(device.deviceMemory !== undefined && device.deviceMemory <= 4) &&
        !device.connection?.saveData;
      setEnhanced(allowed);
      if (!allowed) {
        cancelAnimationFrame(frame);
        frame = 0;
      } else measure();
    };
    update();
    motion.addEventListener("change", update);
    desktop.addEventListener("change", update);
    window.addEventListener("scroll", measure, { passive: true });
    document.addEventListener("visibilitychange", measure);
    return () => {
      cancelAnimationFrame(frame);
      motion.removeEventListener("change", update);
      desktop.removeEventListener("change", update);
      window.removeEventListener("scroll", measure);
      document.removeEventListener("visibilitychange", measure);
    };
  }, []);

  return <EnhancedEffects.Provider value={enhanced}>
    <div className="glass-root" data-effects={enhanced ? "enhanced" : "reduced"}>{children}</div>
  </EnhancedEffects.Provider>;
}
