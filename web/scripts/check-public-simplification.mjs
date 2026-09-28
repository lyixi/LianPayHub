import { chromium } from "playwright";

const browser = await chromium.launch({ headless: true });

async function inspect(viewport) {
  const page = await browser.newPage({ viewport });
  await page.goto("http://0.0.0.0:8888/", { waitUntil: "networkidle" });
  const home = {
    viewport: viewport.width,
    pageWidth: await page.evaluate(() => document.documentElement.scrollWidth),
    sections: await page.locator("main > section").count(),
    navLinks: await page.locator(".nav-glass nav a").allTextContents(),
    removedLabels: await page.getByText(/WHAT WE BUILD|OPTICAL SURFACE|READY TO START/, { exact: false }).count(),
    productCards: await page.locator(".product-feature, .product-item").count(),
  };
  await page.locator(".product-feature").first().click();
  await page.waitForLoadState("networkidle");
  const product = {
    pageWidth: await page.evaluate(() => document.documentElement.scrollWidth),
    title: await page.locator(".product-hero h1").textContent(),
    redundantLearnLink: await page.getByText("了解产品", { exact: true }).count(),
    redundantCta: await page.getByText(/READY TO START|现在开始使用/, { exact: false }).count(),
    downloadActions: await page.getByText("立即下载", { exact: true }).count(),
  };
  await page.close();
  return { home, product };
}

try {
  const desktop = await inspect({ width: 1440, height: 960 });
  const mobile = await inspect({ width: 390, height: 844 });
  for (const result of [desktop, mobile]) {
    if (result.home.pageWidth > result.home.viewport || result.product.pageWidth > result.home.viewport) throw new Error(`页面横向溢出：${JSON.stringify(result)}`);
    if (result.home.removedLabels || result.product.redundantLearnLink || result.product.redundantCta) throw new Error(`仍有冗余元素：${JSON.stringify(result)}`);
  }
  console.log(JSON.stringify({ desktop, mobile }, null, 2));
} finally {
  await browser.close();
}
