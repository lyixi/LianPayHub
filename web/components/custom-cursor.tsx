"use client";

import { useEffect, useRef, useState } from "react";

const INTERACTIVE_SELECTOR = "a, button, input, textarea, select, [role='button'], [data-cursor='interactive']";
const PERFORMANCE_KEY = "lyixi-reduced-effects";

type DeviceNavigator = Navigator & {
  deviceMemory?: number;
  connection?: { saveData?: boolean };
};

export function CustomCursor() {
  const dotRef = useRef<HTMLSpanElement>(null);
  const ringRef = useRef<HTMLSpanElement>(null);
  const target = useRef({ x: -80, y: -80 });
  const current = useRef({ x: -80, y: -80 });
  const frame = useRef<number | null>(null);
  const disabled = useRef(false);
  const previousCursorFrame = useRef(0);
  const slowCursorFrames = useRef(0);
  const [visible, setVisible] = useState(false);
  const [interactive, setInteractive] = useState(false);
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    const finePointer = window.matchMedia("(pointer: fine)");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
    const device = navigator as DeviceNavigator;
    const constrainedDevice =
      reducedMotion.matches ||
      !finePointer.matches ||
      (device.hardwareConcurrency > 0 && device.hardwareConcurrency <= 4) ||
      (device.deviceMemory !== undefined && device.deviceMemory <= 4) ||
      Boolean(device.connection?.saveData) ||
      sessionStorage.getItem(PERFORMANCE_KEY) === "true";

    const reduceEffects = () => {
      disabled.current = true;
      sessionStorage.setItem(PERFORMANCE_KEY, "true");
      document.documentElement.dataset.performance = "reduced";
      delete document.documentElement.dataset.customCursor;
      setEnabled(false);
      setVisible(false);
      if (frame.current !== null) cancelAnimationFrame(frame.current);
      frame.current = null;
    };

    if (constrainedDevice) {
      reduceEffects();
      return;
    }

    setEnabled(true);
    document.documentElement.dataset.customCursor = "true";

    let sampleFrame = 0;
    let previousSample = performance.now();
    let sampleCount = 0;
    let slowFrames = 0;
    let elapsed = 0;
    const samplePerformance = (now: number) => {
      const delta = now - previousSample;
      previousSample = now;
      if (delta > 0 && delta < 250) {
        sampleCount += 1;
        elapsed += delta;
        if (delta > 30) slowFrames += 1;
      }
      if (sampleCount >= 90) {
        const averageFrame = elapsed / sampleCount;
        if (averageFrame > 24 || slowFrames / sampleCount > 0.22) reduceEffects();
        return;
      }
      sampleFrame = requestAnimationFrame(samplePerformance);
    };
    sampleFrame = requestAnimationFrame(samplePerformance);

    const renderRing = (now: number) => {
      if (disabled.current) return;
      if (previousCursorFrame.current > 0) {
        const delta = now - previousCursorFrame.current;
        slowCursorFrames.current = delta > 32
          ? slowCursorFrames.current + 1
          : Math.max(0, slowCursorFrames.current - 1);
        if (slowCursorFrames.current >= 8) {
          reduceEffects();
          return;
        }
      }
      previousCursorFrame.current = now;
      const easing = reducedMotion.matches ? 1 : 0.2;
      current.current.x += (target.current.x - current.current.x) * easing;
      current.current.y += (target.current.y - current.current.y) * easing;
      ringRef.current?.style.setProperty("transform", `translate3d(${current.current.x}px, ${current.current.y}px, 0)`);

      const distance = Math.hypot(target.current.x - current.current.x, target.current.y - current.current.y);
      frame.current = distance > 0.25 ? requestAnimationFrame(renderRing) : null;
    };

    const startRing = () => {
      if (disabled.current) return;
      if (frame.current === null) previousCursorFrame.current = 0;
      if (frame.current === null) frame.current = requestAnimationFrame(renderRing);
    };

    const onMove = (event: PointerEvent) => {
      if (disabled.current) return;
      target.current = { x: event.clientX, y: event.clientY };
      dotRef.current?.style.setProperty("transform", `translate3d(${event.clientX}px, ${event.clientY}px, 0)`);
      setVisible(true);
      startRing();
    };
    const onOver = (event: PointerEvent) => setInteractive(Boolean((event.target as Element | null)?.closest(INTERACTIVE_SELECTOR)));
    const onOut = (event: PointerEvent) => {
      if (!event.relatedTarget) setVisible(false);
      setInteractive(Boolean((event.relatedTarget as Element | null)?.closest?.(INTERACTIVE_SELECTOR)));
    };
    const onBlur = () => setVisible(false);

    window.addEventListener("pointermove", onMove, { passive: true });
    document.addEventListener("pointerover", onOver, { passive: true });
    document.addEventListener("pointerout", onOut, { passive: true });
    window.addEventListener("blur", onBlur);

    return () => {
      delete document.documentElement.dataset.customCursor;
      delete document.documentElement.dataset.performance;
      window.removeEventListener("pointermove", onMove);
      document.removeEventListener("pointerover", onOver);
      document.removeEventListener("pointerout", onOut);
      window.removeEventListener("blur", onBlur);
      cancelAnimationFrame(sampleFrame);
      if (frame.current !== null) cancelAnimationFrame(frame.current);
    };
  }, []);

  if (!enabled) return null;

  return <div className={`cursor ${visible ? "is-visible" : ""} ${interactive ? "is-interactive" : ""}`} aria-hidden="true">
    <span ref={ringRef} className="cursor-ring" />
    <span ref={dotRef} className="cursor-dot" />
  </div>;
}
