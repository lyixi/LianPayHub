"use client";

import { useEffect, useRef, useState } from "react";

const INTERACTIVE_SELECTOR = "a, button, input, textarea, select, [role='button'], [data-cursor='interactive']";

export function CustomCursor() {
  const dotRef = useRef<HTMLSpanElement>(null);
  const ringRef = useRef<HTMLSpanElement>(null);
  const target = useRef({ x: -80, y: -80 });
  const current = useRef({ x: -80, y: -80 });
  const frame = useRef<number | null>(null);
  const [visible, setVisible] = useState(false);
  const [interactive, setInteractive] = useState(false);

  useEffect(() => {
    const finePointer = window.matchMedia("(pointer: fine)");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
    if (!finePointer.matches) return;

    document.documentElement.dataset.customCursor = "true";

    const renderRing = () => {
      const easing = reducedMotion.matches ? 1 : 0.2;
      current.current.x += (target.current.x - current.current.x) * easing;
      current.current.y += (target.current.y - current.current.y) * easing;
      ringRef.current?.style.setProperty("transform", `translate3d(${current.current.x}px, ${current.current.y}px, 0)`);

      const distance = Math.hypot(target.current.x - current.current.x, target.current.y - current.current.y);
      frame.current = distance > 0.25 ? requestAnimationFrame(renderRing) : null;
    };

    const startRing = () => {
      if (frame.current === null) frame.current = requestAnimationFrame(renderRing);
    };

    const onMove = (event: PointerEvent) => {
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
      window.removeEventListener("pointermove", onMove);
      document.removeEventListener("pointerover", onOver);
      document.removeEventListener("pointerout", onOut);
      window.removeEventListener("blur", onBlur);
      if (frame.current !== null) cancelAnimationFrame(frame.current);
    };
  }, []);

  return <div className={`cursor ${visible ? "is-visible" : ""} ${interactive ? "is-interactive" : ""}`} aria-hidden="true">
    <span ref={ringRef} className="cursor-ring" />
    <span ref={dotRef} className="cursor-dot" />
  </div>;
}
