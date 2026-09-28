export type SiteContent = Record<string, string | undefined> & { name: string; heroTitle: string; heroDescription: string; heroLocation: string; featuredProductSlug?: string; heroCarouselJson?: string; companyPrinciplesJson?: string };
export type ProductAsset = { id: number; url: string; title?: string; altText?: string; sortOrder?: number };
export type Product = { id: number; appId?: string; slug: string; name: string; tagline: string; summary?: string; content?: string; downloadUrl?: string; downloadJson?: string; purchaseUrl?: string; coverUrl?: string; mediaJson?: string; previewProject?: string; previewVolume?: string; previewChapter?: string; previewExcerpt?: string; previewInsight?: string; previewChapterTwo?: string; previewExcerptTwo?: string; previewChapterThree?: string; previewExcerptThree?: string; previewBody?: string; status?: string };
export type Article = { id: number; slug: string; category: string; title: string; excerpt?: string; content?: string; coverUrl?: string; seoTitle?: string; seoDescription?: string; publishedAt?: string; updatedAt?: string; status?: string };
export type ContentPage = { id: number; slug: string; title: string; excerpt?: string; content?: string; coverUrl?: string; seoTitle?: string; seoDescription?: string; sortOrder?: number; status?: string; updatedAt?: string };

export async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, options);
  const body = await response.json();
  if (!response.ok || body.code !== 0) throw new Error(body.message || "内容加载失败");
  return body.data;
}
