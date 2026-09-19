import { cp, mkdir } from "node:fs/promises";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "../..");
const output = resolve(root, "web/out");
const staticRoot = resolve(root, "src/main/resources/static");
const runtimeStaticRoot = resolve(root, "target/classes/static");
const siteRoot = resolve(staticRoot, "official-ui");
const adminRoot = resolve(staticRoot, "website-admin");
const assetRoot = resolve(staticRoot, "official-app");

await mkdir(siteRoot, { recursive: true });
await mkdir(adminRoot, { recursive: true });
await mkdir(assetRoot, { recursive: true });
await cp(resolve(output, "index.html"), resolve(siteRoot, "index.html"));
await cp(resolve(output, "admin/index.html"), resolve(adminRoot, "index.html"));
await cp(resolve(output, "_next"), resolve(assetRoot, "_next"), { recursive: true, force: true });

// The local Spring process runs from target/classes during development. Keep
// that runtime classpath in sync when Maven is not available on the machine.
await mkdir(runtimeStaticRoot, { recursive: true });
await cp(staticRoot, runtimeStaticRoot, { recursive: true, force: true });
console.log("Deployed the exported homepage to Spring Boot static resources.");
