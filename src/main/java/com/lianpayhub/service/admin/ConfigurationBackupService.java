package com.lianpayhub.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.StorageProperties;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.zip.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.lianpayhub.service.storage.StorageService;

@Service
public class ConfigurationBackupService {
    private final ConfigurationTransferService configurations;
    private final StorageProperties properties;
    private final StorageService storage;
    private final ObjectMapper mapper;

    public ConfigurationBackupService(ConfigurationTransferService configurations, StorageProperties properties,
            StorageService storage, ObjectMapper mapper) {
        this.configurations = configurations; this.properties = properties; this.storage = storage; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Path exportBackup() throws IOException {
        ObjectNode configuration = configurations.exportConfiguration();
        Path archive = Files.createTempFile("configuration-backup-", ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("configuration.json"));
            zip.write(mapper.writeValueAsBytes(configuration)); zip.closeEntry();
            for (JsonNode media : configuration.path("sections").path("websiteMedia")) {
                String key = media.path("storageKey").asText();
                Path file = resource(key);
                if (!Files.isRegularFile(file)) fail("资源文件缺失: " + key);
                zip.putNextEntry(new ZipEntry("resources/" + key));
                Files.copy(file, zip); zip.closeEntry();
            }
        } catch (Exception ex) { Files.deleteIfExists(archive); throw ex; }
        return archive;
    }

    @Transactional(rollbackFor = Exception.class)
    public ObjectNode importBackup(InputStream input, String strategy, boolean preview) throws IOException {
        Path staging = Files.createTempDirectory("configuration-restore-");
        Map<String, Path> files = new HashMap<>();
        List<Runnable> undo = new ArrayList<>();
        boolean registered = false;
        try {
            ObjectNode configuration = null;
            Set<String> names = new HashSet<>();
            long remaining = 2L * 1024 * 1024 * 1024;
            try (ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (entry.isDirectory()) continue;
                    if (!names.add(name) || names.size() > 10001) fail("重复或过多的备份文件");
                    if (name.equals("configuration.json")) {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        remaining -= copy(zip, bytes, Math.min(remaining, 20L * 1024 * 1024));
                        JsonNode parsed = mapper.readTree(bytes.toByteArray());
                        if (!parsed.isObject()) fail("配置格式无效");
                        configuration = (ObjectNode) parsed;
                    } else if (name.startsWith("resources/")) {
                        String key = name.substring(10); resource(key);
                        Path file = Files.createTempFile(staging, "asset-", ".bin");
                        try (OutputStream out = Files.newOutputStream(file)) { remaining -= copy(zip, out, Math.min(remaining, properties.getMaxDownloadFileBytes())); }
                        files.put(key, file);
                    } else fail("未知备份文件: " + name);
                }
            }
            if (configuration == null) fail("备份缺少配置文件");
            Map<String, String> urls = new HashMap<>();
            Set<String> referenced = new HashSet<>();
            for (JsonNode media : configuration.path("sections").path("websiteMedia")) {
                String key = media.path("storageKey").asText();
                Path file = files.get(key);
                if (!referenced.add(key) || file == null || Files.size(file) != media.path("sizeBytes").asLong()) fail("资源文件缺失、重复或大小不符: " + key);
                urls.put(media.path("url").asText(), storage.getDownloadUrl(key, Duration.ofDays(3650)));
            }
            if (!referenced.equals(files.keySet())) fail("备份包含未登记的资源文件");
            JsonNode sections = configuration.path("sections");
            for (String section : Arrays.asList("websiteMedia", "websiteProducts", "websiteArticles", "websitePages", "websiteSettings", "purchasePages", "products"))
                rewrite(sections.path(section), urls);
            ObjectNode result = configurations.importConfiguration(configuration, strategy, preview);
            ObjectNode report = result.withArray("sections").addObject();
            report.put("section", "resourceFiles"); report.putArray("conflictKeys");
            int added = 0, identical = 0, conflicts = 0, updated = 0;
            // Keep rollback copies until the database transaction completes.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) for (int i = undo.size() - 1; i >= 0; i--) undo.get(i).run();
                    cleanup(staging);
                }
            });
            registered = true;
            for (Map.Entry<String, Path> entry : files.entrySet()) {
                Path target = resource(entry.getKey());
                boolean exists = Files.exists(target);
                if (exists && equal(target, entry.getValue())) { identical++; continue; }
                if (exists) { conflicts++; report.withArray("conflictKeys").add(entry.getKey()); }
                else added++;
                if (exists && !"OVERWRITE".equals(strategy)) continue;
                if (exists) updated++;
                if (preview) continue;
                Path old = exists ? Files.createTempFile(staging, "original-", ".bin") : null;
                if (old != null) Files.copy(target, old, StandardCopyOption.REPLACE_EXISTING);
                undo.add(() -> { try {
                    if (old == null) Files.deleteIfExists(target);
                    else Files.copy(old, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) { throw new UncheckedIOException(ex); } });
                Files.createDirectories(target.getParent());
                Files.copy(entry.getValue(), target, StandardCopyOption.REPLACE_EXISTING);
            }
            report.put("added", added); report.put("identical", identical); report.put("conflicts", conflicts);
            report.put("updated", updated); report.put("keptLocal", conflicts - updated);
            return result;
        } finally { if (!registered) cleanup(staging); }
    }

    private Path resource(String key) throws IOException {
        if (!"local".equals(properties.getBackend()) || !key.startsWith("website/")
                || key.contains("\\") || key.contains("..") || key.endsWith("/")) fail("非法或不支持的资源路径: " + key);
        Path root = Paths.get(properties.getLocalPath()).toAbsolutePath().normalize();
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) fail("非法资源路径");
        for (Path current = target; current != null && current.startsWith(root); current = current.getParent())
            if (Files.isSymbolicLink(current)) fail("资源路径不能是符号链接");
        return target;
    }

    private void rewrite(JsonNode node, Map<String, String> urls) throws IOException {
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            List<String> fields = new ArrayList<>(); object.fieldNames().forEachRemaining(fields::add);
            for (String field : fields) {
                JsonNode value = object.get(field);
                if (!value.isTextual()) { rewrite(value, urls); continue; }
                String text = value.asText();
                if (urls.containsKey(text)) object.put(field, urls.get(text));
                else if ((text.startsWith("{") || text.startsWith("["))) {
                    try {
                        JsonNode nested = mapper.readTree(text), before = nested.deepCopy();
                        rewrite(nested, urls);
                        if (!nested.equals(before)) object.put(field, mapper.writeValueAsString(nested));
                    }
                    catch (com.fasterxml.jackson.core.JsonProcessingException ignored) { }
                } else if (text.contains("<")) {
                    for (Map.Entry<String, String> url : urls.entrySet()) if (!url.getKey().isEmpty())
                        text = text.replace("\"" + url.getKey() + "\"", "\"" + url.getValue() + "\"")
                                .replace("'" + url.getKey() + "'", "'" + url.getValue() + "'");
                    object.put(field, text);
                }
            }
        } else if (node.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode array = (com.fasterxml.jackson.databind.node.ArrayNode) node;
            for (int i = 0; i < array.size(); i++) {
                JsonNode value = array.get(i);
                if (value.isTextual() && urls.containsKey(value.asText())) array.set(i, mapper.valueToTree(urls.get(value.asText())));
                else rewrite(value, urls);
            }
        }
    }

    private static boolean equal(Path first, Path second) throws IOException {
        if (Files.size(first) != Files.size(second)) return false;
        try (InputStream a = Files.newInputStream(first); InputStream b = Files.newInputStream(second)) {
            byte[] x = new byte[8192], y = new byte[8192]; int size;
            while ((size = a.read(x)) != -1) {
                int offset = 0, count;
                while (offset < size && (count = b.read(y, offset, size - offset)) != -1) offset += count;
                if (offset != size || !Arrays.equals(Arrays.copyOf(x, size), Arrays.copyOf(y, size))) return false;
            }
            return true;
        }
    }

    private static long copy(InputStream in, OutputStream out, long limit) throws IOException {
        byte[] bytes = new byte[8192]; long total = 0; int size;
        while ((size = in.read(bytes)) != -1) { total += size; if (total > limit) fail("备份文件超过大小限制"); out.write(bytes, 0, size); }
        return total;
    }
    private static void cleanup(Path directory) {
        try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> { try { Files.deleteIfExists(path); } catch (IOException ex) { throw new UncheckedIOException(ex); } });
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }
    private static void fail(String message) { throw new BusinessException(ErrorCode.BAD_REQUEST, message); }
}
