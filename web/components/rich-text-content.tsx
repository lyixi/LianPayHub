"use client";

import DOMPurify from "dompurify";
import { useMemo } from "react";

function escapeHtml(value: string) {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function normalizeContent(content: string) {
  if (/<[a-z][\s\S]*>/i.test(content)) return content;
  return content.split(/\n{2,}/).filter(Boolean).map(paragraph => `<p>${escapeHtml(paragraph).replace(/\n/g, "<br>")}</p>`).join("");
}

export function RichTextContent({ content, className = "" }: { content?: string; className?: string }) {
  const safeHtml = useMemo(() => DOMPurify.sanitize(normalizeContent(content || ""), {
    USE_PROFILES: { html: true },
    FORBID_TAGS: ["script", "style", "iframe", "object", "embed", "form"],
    FORBID_ATTR: ["style"],
  }), [content]);

  if (!safeHtml) return null;
  return <div className={`rich-text-content ${className}`} dangerouslySetInnerHTML={{ __html: safeHtml }}/>;
}
