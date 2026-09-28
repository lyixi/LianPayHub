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
  publishingHeight: Math.round(document.querySelector(".publishing-overview")?.getBoundingClientRect().height || 0),
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
  const sectionTitles = Array.from(document.querySelectorAll(".product-editor-section>header b")).map(element => element.textContent?.trim());
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
    sectionTitles,
    dropzoneCount: document.querySelectorAll(".asset-dropzone").length,
  };
});
if (editor.sectionTitles[1] !== "产品媒体与下载" || editor.sectionTitles[2] !== "产品正文") throw new Error(`Unexpected product section order: ${JSON.stringify(editor.sectionTitles)}`);
if (editor.dropzoneCount !== 3) throw new Error(`Dropzones are missing: ${JSON.stringify(editor)}`);
const dropzoneActive = await page.locator(".asset-dropzone").first().evaluate(element => {
  element.dispatchEvent(new DragEvent("dragenter", { bubbles: true, cancelable: true, dataTransfer: new DataTransfer() }));
  return new Promise(resolve => requestAnimationFrame(() => resolve(element.classList.contains("is-dragging"))));
});
if (!dropzoneActive) throw new Error("Dropzone did not show drag feedback");
const cursors = await page.evaluate(() => ({
  section: getComputedStyle(document.querySelector(".product-editor-section")).cursor,
  assetField: getComputedStyle(document.querySelector(".asset-field")).cursor,
  dropzone: getComputedStyle(document.querySelector(".asset-dropzone")).cursor,
  uploadButton: getComputedStyle(document.querySelector(".asset-direct-upload")).cursor,
  existingButton: getComputedStyle(document.querySelector(".asset-select-button")).cursor,
  textInput: getComputedStyle(document.querySelector(".product-field-grid input")).cursor,
  dragHandle: getComputedStyle(document.querySelector(".drag-handle")).cursor,
}));
if (cursors.dropzone !== "default" || cursors.uploadButton !== "pointer" || cursors.existingButton !== "pointer" || cursors.textInput !== "text" || cursors.dragHandle !== "grab") throw new Error(`Cursor semantics are incorrect: ${JSON.stringify(cursors)}`);
const gallery = page.locator(".product-asset-stack .asset-field.is-multiple").first();
if (await gallery.locator(".asset-chip").count() === 0) {
  await gallery.getByRole("button", { name: "选择已有资源" }).click();
  const firstImage = gallery.locator(".asset-grid button").first();
  if (await firstImage.count() === 0) throw new Error("No existing image available to verify the gallery's add-more state");
  await firstImage.click();
}
const assetCards = await page.evaluate(() => {
  const selected = document.querySelector(".asset-selected");
  const card = document.querySelector(".asset-chip");
  const preview = card?.querySelector("img,.asset-file-icon");
  const name = card?.querySelector(":scope > span:not(.asset-file-icon)");
  const previewRect = preview?.getBoundingClientRect();
  const nameRect = name?.getBoundingClientRect();
  return {
    layout: selected ? getComputedStyle(selected).display : "missing",
    columns: selected ? getComputedStyle(selected).gridTemplateColumns : "missing",
    coverCards: document.querySelectorAll(".asset-field.is-single .asset-chip").length,
    coverExtraHints: document.querySelectorAll(".asset-field.is-single .asset-drop-hint").length,
    galleryCards: document.querySelectorAll(".product-asset-stack .asset-field.is-multiple .asset-chip").length,
    selectedMultipleHints: document.querySelectorAll(".asset-field.is-multiple .asset-selected.has-selection .asset-drop-hint").length,
    selectedBorderStyle: selected ? getComputedStyle(selected).borderTopStyle : "missing",
    previewWidth: Math.round(previewRect?.width || 0),
    previewHeight: Math.round(previewRect?.height || 0),
    nameBelowPreview: Boolean(previewRect && nameRect && nameRect.top >= previewRect.bottom - 1),
  };
});
if (assetCards.layout !== "grid") throw new Error(`Asset picker is not a grid: ${JSON.stringify(assetCards)}`);
if (assetCards.previewWidth && (assetCards.previewWidth < 120 || assetCards.previewHeight < 70 || !assetCards.nameBelowPreview)) throw new Error(`Asset preview is still too small or mislabeled: ${JSON.stringify(assetCards)}`);
if (assetCards.coverExtraHints !== 0 || (assetCards.coverCards > 0 && assetCards.selectedBorderStyle !== "solid")) throw new Error(`Single-image picker has an extra drop placeholder: ${JSON.stringify(assetCards)}`);
if (assetCards.galleryCards === 0 || assetCards.selectedMultipleHints < 1) throw new Error(`Multi-image picker lost its add-more drop target: ${JSON.stringify(assetCards)}`);
await page.screenshot({ path: "/tmp/lianpayhub-product-assets-mobile.png", fullPage: false });

await page.locator(".product-editor-header .editor-close").click();
await page.getByRole("button", { name: "展开导航" }).click();
await page.locator(".admin-sidebar nav button").filter({ hasText: "首页内容" }).click();
await page.locator(".home-content-editor").waitFor();
const jsonFieldCount = await page.locator("textarea[name='heroCarouselJson']").count();
if (jsonFieldCount) throw new Error("Raw carousel JSON field is still visible");
await page.getByRole("button", { name: "新增一张" }).click();
await page.locator(".carousel-edit-card").last().waitFor();
const contentEditor = await page.evaluate(() => ({
  viewport: document.documentElement.clientWidth,
  pageWidth: document.documentElement.scrollWidth,
  slideCards: document.querySelectorAll(".carousel-edit-card").length,
  typeControls: document.querySelectorAll(".carousel-edit-card select").length,
  stickySavePosition: getComputedStyle(document.querySelector(".content-savebar")).position,
}));
if (contentEditor.pageWidth !== contentEditor.viewport) throw new Error(`Homepage editor overflows: ${JSON.stringify(contentEditor)}`);

await page.screenshot({ path: "/tmp/lianpayhub-product-editor-mobile.png", fullPage: false });
await page.setViewportSize({ width: 1440, height: 1000 });
await page.locator(".admin-sidebar nav button").filter({ hasText: "产品管理" }).click();
await page.locator(".product-admin-row .row-buttons button[title='编辑']").first().click();
await page.locator(".product-editor-section").nth(1).scrollIntoViewIfNeeded();
const desktopGallery = page.locator(".product-asset-stack .asset-field.is-multiple").first();
if (await desktopGallery.locator(".asset-chip").count() === 0) {
  await desktopGallery.getByRole("button", { name: "选择已有资源" }).click();
  await desktopGallery.locator(".asset-grid button").first().click();
}
const desktopAssets = await page.evaluate(() => {
  const fields = Array.from(document.querySelectorAll(".asset-field"));
  return fields.map(field => ({
    width: Math.round(field.getBoundingClientRect().width),
    columns: getComputedStyle(field.querySelector(".asset-selected")).gridTemplateColumns,
    cards: field.querySelectorAll(".asset-chip").length,
    hintCount: field.querySelectorAll(".asset-drop-hint").length,
    cardTop: Math.round(field.querySelector(".asset-chip")?.getBoundingClientRect().top || 0),
    hintTop: Math.round(field.querySelector(".asset-drop-hint")?.getBoundingClientRect().top || 0),
  }));
});
if (desktopAssets[0].hintCount !== 0 || desktopAssets[1].hintCount !== 1 || desktopAssets[1].cardTop !== desktopAssets[1].hintTop) throw new Error(`Desktop upload cards do not share a row: ${JSON.stringify(desktopAssets)}`);
await page.screenshot({ path: "/tmp/lianpayhub-product-assets-desktop.png", fullPage: false });
console.log(JSON.stringify({ dashboard, navigation: { openTransform, closedTransform }, products, editor: { ...editor, cursors, assetCards }, contentEditor, desktopAssets }, null, 2));
await browser.close();
