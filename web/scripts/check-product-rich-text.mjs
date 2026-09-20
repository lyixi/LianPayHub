import { chromium } from "playwright";

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1440, height: 960 }, deviceScaleFactor: 1 });
let original;
let token;

async function adminRequest(path, options = {}) {
  return page.evaluate(async ({ path, token, options }) => {
    const response = await fetch(path, {
      ...options,
      headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}`, ...(options.headers || {}) },
    });
    const body = await response.json();
    if (!response.ok || body.code !== 0) throw new Error(body.message || `Request failed: ${path}`);
    return body.data;
  }, { path, token, options });
}

try {
  await page.goto("http://0.0.0.0:8888/admin/", { waitUntil: "networkidle" });
  await page.getByRole("button", { name: "验证并进入" }).click();
  await page.locator(".admin-shell").waitFor();
  token = await page.evaluate(() => localStorage.getItem("websiteAdminToken"));

  const products = await adminRequest("/admin/website/products");
  original = products.find(product => product.status === "PUBLISHED") || products[0];
  if (!original) throw new Error("No product is available for the rich-text check");

  const marker = `富文本回归验证-${Date.now()}`;
  const testContent = [
    `<h2>${marker}</h2>`,
    "<p>这是一段包含 <strong>重点信息</strong> 与 <a href=\"https://lyixi.com\">产品链接</a> 的正文。</p>",
    "<img src=\"/rich-text-check.webp\" alt=\"富文本图片验证\">",
    "<table><tbody><tr><th>平台</th><th>状态</th></tr><tr><td>Windows</td><td>可用</td></tr></tbody></table>",
    "<script>window.__richTextUnsafe = true</script>",
  ].join("");

  await adminRequest(`/admin/website/products/${original.id}`, { method: "PUT", body: JSON.stringify({ ...original, content: testContent }) });
  await page.reload({ waitUntil: "networkidle" });
  await page.getByRole("button", { name: "产品管理" }).click();
  const row = page.locator(".product-admin-row").filter({ hasText: original.name });
  await row.locator("button[title='编辑']").click();
  await page.locator(".rich-editor-content").waitFor();

  const editor = {
    heading: await page.locator(".rich-editor-content h2").filter({ hasText: marker }).count(),
    image: await page.locator(".rich-editor-content img[alt='富文本图片验证']").count(),
    table: await page.locator(".rich-editor-content table").count(),
  };
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await page.getByText("产品已保存").waitFor();

  const savedProducts = await adminRequest("/admin/website/products");
  const saved = savedProducts.find(product => product.id === original.id);
  await page.goto(`http://0.0.0.0:8888/product/${original.slug}`, { waitUntil: "networkidle" });
  await page.locator(".rich-text-content").waitFor();
  const publicPage = {
    heading: await page.locator(".rich-text-content h2").filter({ hasText: marker }).count(),
    strong: await page.locator(".rich-text-content strong").count(),
    image: await page.locator(".rich-text-content img[alt='富文本图片验证']").count(),
    table: await page.locator(".rich-text-content table").count(),
    script: await page.locator(".rich-text-content script").count(),
    unsafeGlobal: await page.evaluate(() => Boolean(window.__richTextUnsafe)),
  };

  if (!saved?.content?.includes(marker)) throw new Error("Saved product content does not contain the test marker");
  if (Object.values(editor).some(count => count !== 1)) throw new Error(`Editor did not restore rich content: ${JSON.stringify(editor)}`);
  if ([publicPage.heading, publicPage.strong, publicPage.image, publicPage.table].some(count => count !== 1)) throw new Error(`Public product page lost rich content: ${JSON.stringify(publicPage)}`);
  if (publicPage.script !== 0 || publicPage.unsafeGlobal) throw new Error(`Unsafe rich text was executed: ${JSON.stringify(publicPage)}`);

  console.log(JSON.stringify({ product: original.slug, editor, publicPage, saved: true }, null, 2));
} finally {
  if (original && token) {
    await adminRequest(`/admin/website/products/${original.id}`, { method: "PUT", body: JSON.stringify(original) }).catch(error => {
      console.error(`Failed to restore product ${original.id}:`, error);
      process.exitCode = 1;
    });
  }
  await browser.close();
}
