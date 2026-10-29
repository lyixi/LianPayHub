package com.lianpayhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lianpayhub.domain.app.*;
import com.lianpayhub.domain.product.*;
import com.lianpayhub.domain.packageinfo.*;
import com.lianpayhub.domain.payment.*;
import com.lianpayhub.repository.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import com.lianpayhub.domain.website.WebsiteMedia;
import com.lianpayhub.config.StorageProperties;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MySqlIntegrationTest
@SpringBootTest(properties = {
        "spring.jpa.show-sql=false", "logging.level.root=WARN",
        "spring.main.banner-mode=off", "spring.jpa.open-in-view=false",
        "lianpayhub.security.jwt-secret=test-jwt-secret-for-configuration-transfer",
        "lianpayhub.storage.local-path=target/configuration-transfer-test-assets",
        "lianpayhub.admin.default-username=admin", "lianpayhub.admin.default-password=admin123456"
})
@AutoConfigureMockMvc
class ConfigurationTransferIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AppInfoRepository apps;
    @Autowired ProductInfoRepository products;
    @Autowired ProductPlanRepository plans;
    @Autowired PackageInfoRepository packages;
    @Autowired PaymentChannelConfigRepository payments;
    @Autowired AdminOperationLogRepository logs;
    @Autowired WebsiteMediaRepository media;
    @Autowired WebsiteProductRepository websiteProducts;
    @Autowired StorageProperties storageProperties;

    @Test
    void configurationRoundTripShouldMergeRemapSkipPreviewAndRollback() throws Exception {
        mvc.perform(get("/admin/configuration-transfer/export")).andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/configuration-transfer/import").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        String token = mapper.readTree(mvc.perform(post("/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("data").path("token").asText();
        apps.saveAndFlush(new AppInfo("sync-source", "Source", "source-hash", AppType.STANDARD, true, false));
        PackageInfo pack = packages.saveAndFlush(new PackageInfo("sync-source", "Month", PackageType.MEMBERSHIP, 100, 30, "Benefits"));
        ProductInfo product = products.saveAndFlush(new ProductInfo("sync-source", "vip", "VIP", ProductType.VIP,
                FulfillmentType.MEMBERSHIP_DURATION, null, "Benefits", 0));
        plans.saveAndFlush(new ProductPlan(product.getId(), "sync-source", "month", "Month", 100, null, 30,
                null, null, "Benefits", 0, pack.getId()));
        payments.saveAndFlush(new PaymentChannelConfig("sync-source", PayChannel.ALIPAY, "alipay", "merchant", null,
                "https://example.com/notify", "{}", "{\"privateKey\":\"transfer-secret\"}"));

        ObjectNode full = export(token);
        assertEquals(16, full.path("sections").size());
        JsonNode same = transfer(token, full, "KEEP_LOCAL", false, 200);
        for (JsonNode section : same.path("sections")) {
            assertEquals(0, section.path("added").asInt());
            assertEquals(0, section.path("conflicts").asInt(), section.toString());
        }

        ObjectNode remote = full.deepCopy();
        ObjectNode data = (ObjectNode) remote.path("sections");
        for (String section : Arrays.asList("apps", "packages", "products", "plans", "paymentChannels")) {
            ArrayNode selected = mapper.createArrayNode();
            for (JsonNode row : data.path(section)) {
                if (row.path("appId").asText().equals("sync-source")) {
                    ObjectNode copy = row.deepCopy();
                    copy.put("appId", "sync-target");
                    selected.add(copy);
                }
            }
            data.set(section, selected);
        }
        data.retain("apps", "packages", "products", "plans", "paymentChannels");
        long appCount = apps.count();
        JsonNode preview = transfer(token, remote, "KEEP_LOCAL", true, 200);
        assertEquals(1, report(preview, "plans").path("added").asInt());
        assertEquals(appCount, apps.count());
        assertFalse(apps.existsByAppId("sync-target"));
        transfer(token, remote, "KEEP_LOCAL", false, 200);
        ProductInfo targetProduct = products.findByAppIdAndProductCode("sync-target", "vip").get();
        ProductPlan targetPlan = plans.findByProductIdAndPlanCode(targetProduct.getId(), "month").get();
        assertNotEquals(product.getId(), targetPlan.getProductId());
        assertNotEquals(pack.getId(), targetPlan.getLegacyPackageId());
        assertEquals("sync-target", packages.findById(targetPlan.getLegacyPackageId()).get().getAppId());
        assertEquals("{\"privateKey\":\"transfer-secret\"}", payments.findByAppIdAndPayChannel("sync-target", PayChannel.ALIPAY).get().getCredentialJson());
        Object timestamp = targetPlan.getUpdatedAt();
        JsonNode repeated = transfer(token, remote, "OVERWRITE", false, 200);
        assertEquals(1, report(repeated, "plans").path("identical").asInt());
        assertEquals(timestamp, plans.findById(targetPlan.getId()).get().getUpdatedAt());

        ((ObjectNode) data.path("apps").get(0)).put("appName", "Remote name");
        JsonNode kept = transfer(token, remote, "KEEP_LOCAL", false, 200);
        assertEquals(1, report(kept, "apps").path("keptLocal").asInt());
        assertEquals("Source", apps.findByAppId("sync-target").get().getAppName());
        transfer(token, remote, "OVERWRITE", true, 200);
        assertEquals("Source", apps.findByAppId("sync-target").get().getAppName());
        transfer(token, remote, "OVERWRITE", false, 200);
        assertEquals("Remote name", apps.findByAppId("sync-target").get().getAppName());

        ObjectNode invalid = remote.deepCopy();
        ((ObjectNode) invalid.path("sections").path("apps").get(0)).put("appName", "Must rollback");
        ((ObjectNode) invalid.path("sections").path("plans").get(0)).put("productId", 999999);
        transfer(token, invalid, "OVERWRITE", false, 400);
        assertEquals("Remote name", apps.findByAppId("sync-target").get().getAppName());
        invalid = remote.deepCopy();
        ((ArrayNode) invalid.path("sections").path("apps")).add(invalid.path("sections").path("apps").get(0).deepCopy());
        transfer(token, invalid, "OVERWRITE", false, 400);
        invalid = remote.deepCopy();
        invalid.put("version", 2);
        transfer(token, invalid, "KEEP_LOCAL", true, 400);
        invalid = remote.deepCopy();
        ((ObjectNode) invalid.path("sections").path("apps").get(0)).put("appType", "INVALID");
        transfer(token, invalid, "KEEP_LOCAL", true, 400);
        for (com.lianpayhub.domain.log.AdminOperationLog log : logs.findAll()) {
            assertFalse(String.valueOf(log.getRequestBody()).contains("transfer-secret"));
            assertFalse(String.valueOf(log.getRequestBody()).contains("source-hash"));
        }
        resourceBackupShouldRestoreAndMerge(token);
    }

    private void resourceBackupShouldRestoreAndMerge(String token) throws Exception {
        Path root = Paths.get(storageProperties.getLocalPath());
        Path asset = root.resolve("website/backup-cover.png");
        Files.createDirectories(asset.getParent());
        byte[] original = new byte[] { 1, 2, 3, 4 };
        Files.write(asset, original);
        storageProperties.setLocalBaseUrl("https://source.example/files");
        WebsiteMedia sourceMedia = media.saveAndFlush(new WebsiteMedia("website/backup-cover.png", "https://source.example/files/website/backup-cover.png",
                "cover.png", "image/png", 4L, "Cover", "Cover alt", "Description"));
        com.lianpayhub.domain.website.WebsiteProduct websiteProduct = websiteProducts.saveAndFlush(
                new com.lianpayhub.domain.website.WebsiteProduct("backup-product", null, "Backup product", "Tagline", null, null,
                        "https://source.example/files/website/backup-cover.png", null, null,
                        "<img src=\"https://source.example/files/website/backup-cover.png\">", 0));
        String assets = "[{\"id\":" + sourceMedia.getId() + ",\"url\":\"" + sourceMedia.getUrl() + "\"}]";
        websiteProduct.update(null, "Backup product", "Tagline", null, null, sourceMedia.getUrl(), sourceMedia.getUrl(), assets,
                null, websiteProduct.getContent(), assets, 0);
        websiteProducts.saveAndFlush(websiteProduct);
        byte[] backup = mvc.perform(get("/admin/configuration-transfer/backup").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().contentType("application/zip"))
                .andReturn().getResponse().getContentAsByteArray();
        media.deleteAll(); websiteProducts.deleteById(websiteProduct.getId()); Files.delete(asset);
        storageProperties.setLocalBaseUrl("/target-files");
        JsonNode preview = restore(token, backup, true, "KEEP_LOCAL", 0);
        assertEquals(1, report(preview, "resourceFiles").path("added").asInt());
        assertEquals(0, media.count()); assertFalse(Files.exists(asset));
        restore(token, backup, false, "KEEP_LOCAL", 0);
        assertArrayEquals(original, Files.readAllBytes(asset));
        assertEquals("/target-files/website/backup-cover.png", media.findAll().get(0).getUrl());
        com.lianpayhub.domain.website.WebsiteProduct restoredProduct = websiteProducts.findBySlug("backup-product").get();
        assertEquals("/target-files/website/backup-cover.png", restoredProduct.getCoverUrl());
        assertEquals("<img src=\"/target-files/website/backup-cover.png\">", restoredProduct.getContent());
        long targetMediaId = media.findAll().get(0).getId();
        assertNotEquals(sourceMedia.getId().longValue(), targetMediaId);
        assertEquals(targetMediaId, mapper.readTree(restoredProduct.getMediaJson()).get(0).path("id").asLong());
        assertEquals(targetMediaId, mapper.readTree(restoredProduct.getDownloadJson()).get(0).path("id").asLong());
        JsonNode identical = restore(token, backup, false, "OVERWRITE", 0);
        assertEquals(1, report(identical, "websiteMedia").path("identical").asInt());
        assertEquals(1, report(identical, "resourceFiles").path("identical").asInt());
        Files.write(asset, new byte[] { 9, 8, 7, 6 });
        JsonNode conflict = restore(token, backup, false, "KEEP_LOCAL", 0);
        assertEquals(1, report(conflict, "resourceFiles").path("keptLocal").asInt());
        assertEquals(9, Files.readAllBytes(asset)[0]);
        restore(token, backup, false, "OVERWRITE", 0);
        assertArrayEquals(original, Files.readAllBytes(asset));
        java.io.ByteArrayOutputStream malformed = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(malformed)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("configuration.json"));
            zip.write(mapper.writeValueAsBytes(export(token))); zip.closeEntry();
        }
        restore(token, malformed.toByteArray(), false, "OVERWRITE", 400);
        assertArrayEquals(original, Files.readAllBytes(asset));
        media.deleteAll(); websiteProducts.deleteById(restoredProduct.getId()); Files.delete(asset);
    }

    private JsonNode restore(String token, byte[] backup, boolean preview, String strategy, int code) throws Exception {
        JsonNode response = mapper.readTree(mvc.perform(multipart("/admin/configuration-transfer/restore")
                .file(new MockMultipartFile("file", "backup.zip", "application/zip", backup))
                .header("Authorization", "Bearer " + token).param("preview", String.valueOf(preview)).param("strategy", strategy))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(code, response.path("code").asInt(), response.toString());
        return response.path("data");
    }

    private ObjectNode export(String token) throws Exception {
        return (ObjectNode) mapper.readTree(mvc.perform(get("/admin/configuration-transfer/export").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode transfer(String token, JsonNode configuration, String strategy, boolean preview, int status) throws Exception {
        JsonNode response = mapper.readTree(mvc.perform(post("/admin/configuration-transfer/import")
                .header("Authorization", "Bearer " + token).param("strategy", strategy).param("preview", String.valueOf(preview))
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(configuration)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(status == 200 ? 0 : status, response.path("code").asInt(), response.toString());
        return response.path("data");
    }

    private JsonNode report(JsonNode result, String name) {
        for (JsonNode section : result.path("sections")) if (section.path("section").asText().equals(name)) return section;
        throw new AssertionError("Missing section " + name);
    }
}
