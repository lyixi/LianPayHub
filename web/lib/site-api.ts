export type SiteContent = Record<string, string | undefined> & { name: string; heroTitle: string; heroDescription: string; heroLocation: string; featuredProductSlug?: string };
export type ProductAsset = { id: number; url: string; title?: string; altText?: string; sortOrder?: number };
export type Product = { id: number; slug: string; name: string; tagline: string; summary?: string; content?: string; downloadUrl?: string; purchaseUrl?: string; coverUrl?: string; mediaJson?: string; previewProject?: string; previewVolume?: string; previewChapter?: string; previewExcerpt?: string; previewInsight?: string; previewChapterTwo?: string; previewExcerptTwo?: string; previewChapterThree?: string; previewExcerptThree?: string; previewBody?: string };
export type Article = { id: number; slug: string; category: string; title: string; excerpt?: string; content?: string; coverUrl?: string; seoTitle?: string; seoDescription?: string; publishedAt?: string; updatedAt?: string };

export async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, options);
  const body = await response.json();
  if (!response.ok || body.code !== 0) throw new Error(body.message || "内容加载失败");
  return body.data;
}
