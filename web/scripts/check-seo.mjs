import assert from "node:assert/strict";
import { chromium } from "playwright";

const baseUrl = process.env.SITE_BASE_URL || "http://127.0.0.1:8888";
const browser = await chromium.launch({ headless: true });
const results = [];

try {
  const response = await fetch(`${baseUrl}/api/site/v1/products`);
  const { data: products } = await response.json();
  assert.ok(products.length, "A published product is required for the SEO check");
  const product = products[0];
  const productPath = `/product/${product.slug}`;

  const noJs = await browser.newContext({ javaScriptEnabled: false });
  const page = await noJs.newPage();
  const sitemapResponse = await fetch(`${baseUrl}/sitemap.xml`);
  assert.equal(sitemapResponse.status, 200, "Sitemap must be reachable");
  const sitemap = await sitemapResponse.text();
  const sitemapPaths = [...sitemap.matchAll(/<loc>https?:\/\/[^/]+([^<]*)<\/loc>/g)].map(match => match[1] || "/");
  assert.ok(sitemapPaths.length, "Sitemap must contain public URLs");
  for (const path of sitemapPaths) {
    const document = await page.goto(`${baseUrl}${path}`);
    assert.equal(document.status(), 200, `${path} must return HTTP 200`);
    assert.equal(await page.locator("#seo-content").isVisible(), true, `${path} must expose readable content without JavaScript`);
    assert.equal(await page.locator("#seo-content h1").count(), 1, `${path} must have one crawler-visible H1`);
    assert.equal(await page.locator("title").count(), 1, `${path} must have one title`);
    assert.equal(await page.locator('meta[name="description"]').count(), 1, `${path} must have one description`);
    assert.ok((await page.locator('meta[name="description"]').getAttribute("content"))?.trim(), `${path} description must not be empty`);
    assert.equal(await page.locator('link[rel="canonical"]').count(), 1, `${path} must have one canonical`);
  }
  for (const item of products) {
    const document = await page.goto(`${baseUrl}/product/${item.slug}`);
    assert.equal(document.status(), 200);
    assert.equal(await page.locator("#seo-content h1").textContent(), item.name);
    assert.equal(await page.locator("#seo-content").isVisible(), true);
    assert.equal(await page.locator("title").count(), 1);
    assert.equal(await page.locator('link[rel="canonical"]').count(), 1);
  }
  await page.goto(baseUrl);
  assert.equal(await page.locator(`#seo-content a[href="${productPath}"]`).count(), 1);
  await noJs.close();
  results.push({ check: "all sitemap URLs have crawler-readable content and complete metadata", count: sitemapPaths.length });
  results.push({ check: "published products readable without JavaScript", count: products.length });

  const context = await browser.newContext();
  const interactive = await context.newPage();
  const errors = [];
  interactive.on("pageerror", error => errors.push(error.message));
  for (const viewport of [{ width: 1440, height: 960 }, { width: 390, height: 844 }]) {
    await interactive.setViewportSize(viewport);
    await interactive.goto(`${baseUrl}${productPath}`);
    await interactive.locator('html[data-site-ready="true"]').waitFor();
    assert.equal(await interactive.locator("#seo-content").isVisible(), false);
    assert.equal(await interactive.locator(".product-page h1").textContent(), product.name);
    assert.equal(await interactive.locator(".product-page").isVisible(), true);
    assert.equal(await interactive.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
    await interactive.screenshot({ path: `/tmp/lyixi-seo-${viewport.width}.png`, fullPage: true });
  }
  for (const path of ["/", "/product", "/news", "/about"]) {
    await interactive.goto(`${baseUrl}${path}`);
    await interactive.locator('html[data-site-ready="true"]').waitFor();
    assert.equal(await interactive.locator("#seo-content").isVisible(), false);
  }
  assert.deepEqual(errors, [], "Client hydration must not produce errors");
  await context.close();
  results.push({ check: "interactive pages and desktop/mobile handoff", passed: true });

  const unrelatedFailure = await browser.newContext();
  await unrelatedFailure.route("**/api/site/v1/articles**", route => route.abort());
  await unrelatedFailure.route("**/api/site/v1/pages**", route => route.abort());
  await unrelatedFailure.route("**/api/site/v1/site", route => route.abort());
  const independentProduct = await unrelatedFailure.newPage();
  await independentProduct.goto(`${baseUrl}${productPath}`);
  await independentProduct.locator('html[data-site-ready="true"]').waitFor();
  assert.equal(await independentProduct.locator(".product-page h1").textContent(), product.name);
  await unrelatedFailure.close();
  results.push({ check: "product page survives unrelated API failures", passed: true });

  const failedApi = await browser.newContext();
  await failedApi.route("**/api/site/v1/**", route => route.abort());
  const fallback = await failedApi.newPage();
  await fallback.goto(`${baseUrl}${productPath}`, { waitUntil: "networkidle" });
  assert.equal(await fallback.locator("#seo-content").isVisible(), true);
  assert.equal(await fallback.locator("#seo-content h1").textContent(), product.name);
  assert.equal(await fallback.locator("html[data-site-ready]").count(), 0);
  await failedApi.close();
  results.push({ check: "content remains visible when APIs fail", passed: true });
  console.log(JSON.stringify(results, null, 2));
} finally {
  await browser.close();
}
