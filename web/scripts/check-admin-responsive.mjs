import { chromium } from "playwright";

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 1 });

await page.goto("http://0.0.0.0:8888/admin/", { waitUntil: "networkidle" });
await page.getByRole("button", { name: "验证并进入" }).click();
await page.locator(".admin-shell").waitFor();

const dashboard = await page.evaluate(() => ({
  viewport: document.documentElement.clientWidth,
  pageWidth: document.documentElement.scrollWidth,
  metricHeights: Array.from(document.querySelectorAll(".admin-metrics .admin-card")).map(element => Math.round(element.getBoundingClientRect().height)),
  chartHeight: Math.round(document.querySelector(".chart-card")?.getBoundingClientRect().height || 0),
}));

await page.getByRole("button", { name: "展开导航" }).click();
await page.waitForTimeout(800);
const openTransform = await page.locator(".admin-sidebar").evaluate(element => getComputedStyle(element).transform);
await page.getByRole("button", { name: "收起侧边导航" }).click({ position: { x: 370, y: 420 } });
await page.waitForTimeout(800);
const closedTransform = await page.locator(".admin-sidebar").evaluate(element => getComputedStyle(element).transform);

await page.getByRole("button", { name: "展开导航" }).click();
await page.waitForTimeout(800);
await page.locator(".admin-sidebar nav button").filter({ hasText: "产品管理" }).click();
await page.locator(".product-admin-row").first().waitFor();
const products = await page.evaluate(() => ({
  viewport: document.documentElement.clientWidth,
  pageWidth: document.documentElement.scrollWidth,
  rowWidth: Math.round(document.querySelector(".product-admin-row")?.getBoundingClientRect().width || 0),
  tableHeadVisible: getComputedStyle(document.querySelector(".admin-table-head")).display,
}));

await page.locator(".product-admin-row .row-buttons button[title='编辑']").first().click();
await page.locator(".rich-editor-content").waitFor();
const editorScroll = await page.locator(".product-editor-fields").evaluate(element => {
  const before = element.scrollTop;
  element.scrollTop = 420;
  return new Promise(resolve => requestAnimationFrame(() => resolve({ before, after: element.scrollTop })));
});
if (editorScroll.after < 300) throw new Error(`Product editor did not scroll: ${JSON.stringify(editorScroll)}`);
const editor = await page.evaluate(() => {
  const drawer = document.querySelector(".product-editor-drawer");
  const header = document.querySelector(".product-editor-header");
  const fields = document.querySelector(".product-editor-fields");
  return {
    drawerHeight: Math.round(drawer?.getBoundingClientRect().height || 0),
    headerHeight: Math.round(header?.getBoundingClientRect().height || 0),
    footerCount: drawer?.querySelectorAll(":scope > form > footer").length || 0,
    fieldsClientHeight: fields?.clientHeight || 0,
    fieldsScrollHeight: fields?.scrollHeight || 0,
    fieldsOverflowY: fields ? getComputedStyle(fields).overflowY : "missing",
    toolbarHeight: Math.round(document.querySelector(".rich-toolbar")?.getBoundingClientRect().height || 0),
    pageWidth: document.documentElement.scrollWidth,
    viewport: document.documentElement.clientWidth,
    scrollTop: document.querySelector(".product-editor-fields")?.scrollTop || 0,
  };
});

await page.screenshot({ path: "/tmp/lianpayhub-product-editor-mobile.png", fullPage: false });
console.log(JSON.stringify({ dashboard, navigation: { openTransform, closedTransform }, products, editor }, null, 2));
await browser.close();
