package com.lianpayhub.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.ai.*;
import com.lianpayhub.domain.notification.NotificationChannelConfig;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.product.*;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.purchase.PurchasePageConfig;
import com.lianpayhub.domain.search.SearchPlatformConfig;
import com.lianpayhub.domain.website.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Service;
import com.lianpayhub.config.DefaultAdminProperties;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationTransferService {
    private static final String FORMAT = "lianpayhub-configuration";
    // Order matters: referenced records must be matched before their dependants.
    private static final List<Section> SECTIONS = Arrays.asList(
            new Section("apps", AppInfo.class, "appId"),
            new Section("aiProviders", AiProviderConfig.class, "providerCode"),
            new Section("searchPlatforms", SearchPlatformConfig.class, "providerCode"),
            new Section("notificationChannels", NotificationChannelConfig.class, "channelType", "providerCode"),
            new Section("paymentChannels", PaymentChannelConfig.class, "appId", "payChannel"),
            new Section("platformPolicies", AppPlatformPolicy.class, "appId", "category"),
            new Section("appAiSettings", AppAiProviderSetting.class, "appId", "providerCode"),
            new Section("packages", PackageInfo.class, "appId", "packageType", "packageName"),
            new Section("products", ProductInfo.class, "appId", "productCode"),
            new Section("plans", ProductPlan.class, "productId", "planCode"),
            new Section("purchasePages", PurchasePageConfig.class, "pageSlug"),
            new Section("websiteMedia", WebsiteMedia.class, "storageKey"),
            new Section("websiteProducts", WebsiteProduct.class, "slug"),
            new Section("websiteArticles", WebsiteArticle.class, "slug"),
            new Section("websitePages", WebsitePage.class, "slug"),
            new Section("websiteSettings", WebsiteSiteSettings.class));

    private final EntityManager em;
    private final ObjectMapper mapper;
    private final DefaultAdminProperties adminProperties;

    public ConfigurationTransferService(EntityManager em, ObjectMapper mapper, DefaultAdminProperties adminProperties) {
        this.em = em;
        this.mapper = mapper;
        this.adminProperties = adminProperties;
    }

    @Transactional(readOnly = true)
    public ObjectNode exportConfiguration() {
        if (!adminProperties.isExportEnabled()) throw new BusinessException(ErrorCode.FORBIDDEN, "后台导出功能已关闭");
        ObjectNode root = mapper.createObjectNode();
        root.put("format", FORMAT);
        root.put("version", 1);
        root.put("exportedAt", LocalDateTime.now().toString());
        ObjectNode data = root.putObject("sections");
        for (Section section : SECTIONS) {
            ArrayNode rows = data.putArray(section.name);
            for (Object entity : entities(section)) rows.add(encode(section, entity));
        }
        return root;
    }

    @Transactional
    public ObjectNode importConfiguration(JsonNode root, String strategy, boolean preview) {
        if (!"KEEP_LOCAL".equals(strategy) && !"OVERWRITE".equals(strategy)) fail("Invalid conflict strategy");
        if (root == null || !FORMAT.equals(root.path("format").asText())
                || !root.path("version").isIntegralNumber() || !root.path("version").canConvertToInt() || root.path("version").intValue() != 1
                || !root.path("sections").isObject()) fail("Invalid configuration format or version");
        JsonNode data = root.path("sections");
        Set<String> allowed = new HashSet<>();
        for (Section section : SECTIONS) allowed.add(section.name);
        data.fieldNames().forEachRemaining(name -> { if (!allowed.contains(name)) fail("Unknown section: " + name); });
        ObjectNode result = mapper.createObjectNode();
        result.put("preview", preview);
        result.put("strategy", strategy);
        ArrayNode reports = result.putArray("sections");
        Map<String, Map<Long, Long>> ids = new HashMap<>();
        Set<String> apps = new HashSet<>();
        Map<String, Map<Long, String>> referenceApps = new HashMap<>();
        for (Object app : entities(SECTIONS.get(0))) apps.add(((AppInfo) app).getAppId());
        long temporaryId = -1;
        for (Section section : SECTIONS) {
            JsonNode rows = data.get(section.name);
            if (rows == null) continue;
            if (!rows.isArray() || rows.size() > 10000) fail("Invalid section: " + section.name);
            Map<String, ObjectNode> local = new HashMap<>();
            for (Object entity : entities(section)) {
                ObjectNode row = encode(section, entity);
                if (local.put(key(section, row), row) != null) fail("Ambiguous local records: " + section.name);
            }
            Map<Long, Long> sectionIds = new HashMap<>();
            ids.put(section.name, sectionIds);
            Map<Long, String> sectionApps = new HashMap<>();
            referenceApps.put(section.name, sectionApps);
            Set<String> seen = new HashSet<>();
            ObjectNode report = reports.addObject();
            report.put("section", section.name);
            int added = 0, identical = 0, conflicts = 0, updated = 0;
            ArrayNode conflictKeys = report.putArray("conflictKeys");
            ObjectNode conflictFields = report.putObject("conflictFields");
            for (JsonNode source : rows) {
                ObjectNode row = validate(section, source);
                Long sourceId = row.path("id").longValue();
                if (sectionIds.containsKey(sourceId)) fail("Duplicate source ID: " + section.name);
                if (section.name.equals("plans")) {
                    checkReferenceApp(row, "productId", referenceApps.get("products"));
                    if (row.hasNonNull("legacyPackageId")) checkReferenceApp(row, "legacyPackageId", referenceApps.get("packages"));
                    remap(row, "productId", ids.get("products"), false);
                    remap(row, "legacyPackageId", ids.get("packages"), true);
                }
                if (section.name.equals("websiteProducts")) {
                    remapMedia(row, "mediaJson", ids.get("websiteMedia"));
                    remapMedia(row, "downloadJson", ids.get("websiteMedia"));
                }
                if (section.name.equals("apps")) apps.add(row.path("appId").asText());
                if (row.hasNonNull("appId") && !apps.contains(row.path("appId").asText())) fail("Unknown appId in " + section.name);
                String key = key(section, row);
                if (!seen.add(key)) fail("Duplicate business key in file: " + section.name + " " + key);
                ObjectNode existing = local.get(key);
                long targetId;
                boolean write;
                if (existing == null) {
                    added++;
                    targetId = section.name.equals("websiteSettings") ? 1 : temporaryId--;
                    write = true;
                } else {
                    targetId = existing.path("id").longValue();
                    if (content(row).equals(content(existing))) {
                        identical++;
                        write = false;
                    } else {
                        conflicts++;
                        conflictKeys.add(key);
                        ArrayNode changed = conflictFields.putArray(key);
                        row.fieldNames().forEachRemaining(field -> {
                            if (!field.equals("id") && !Objects.equals(row.get(field), existing.get(field))) changed.add(field);
                        });
                        write = "OVERWRITE".equals(strategy);
                        if (write) updated++;
                    }
                }
                if (write && !preview) {
                    Object entity = existing == null ? instantiate(section.type) : em.find(section.type, targetId);
                    fill(section, entity, row);
                    if (existing == null) {
                        if (section.name.equals("websiteSettings")) setId(section, entity, 1L);
                        em.persist(entity);
                    }
                    em.flush();
                    targetId = encode(section, entity).path("id").longValue();
                }
                sectionIds.put(sourceId, targetId);
                sectionApps.put(sourceId, row.path("appId").asText());
                row.put("id", targetId);
                if (existing == null || write) local.put(key, row);
            }
            report.put("added", added);
            report.put("identical", identical);
            report.put("conflicts", conflicts);
            report.put("updated", updated);
            report.put("keptLocal", conflicts - updated);
        }
        return result;
    }

    private List<?> entities(Section section) {
        return em.createQuery("select e from " + section.type.getSimpleName() + " e order by e.id").getResultList();
    }

    private ObjectNode encode(Section section, Object entity) {
        ObjectNode row = mapper.createObjectNode();
        try {
            for (Field field : section.type.getDeclaredFields()) {
                if (!field.getName().equals("id") && field.getAnnotation(Column.class) == null) continue;
                field.setAccessible(true);
                row.set(field.getName(), mapper.valueToTree(field.get(entity)));
            }
        } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
        return row;
    }

    private ObjectNode validate(Section section, JsonNode source) {
        if (!source.isObject() || !source.path("id").isIntegralNumber()
                || !source.path("id").canConvertToLong() || source.path("id").longValue() < 1) fail("Invalid record ID: " + section.name);
        ObjectNode row = (ObjectNode) source.deepCopy();
        Set<String> fields = new HashSet<>();
        fields.add("id");
        for (Field field : section.type.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) continue;
            String name = field.getName();
            fields.add(name);
            JsonNode value = row.get(name);
            if (value == null || (value.isNull() && (!column.nullable() || field.getType().isPrimitive()))) fail("Missing field: " + section.name + "." + name);
            if (value.isNull()) continue;
            Class<?> type = field.getType();
            if (type == String.class) {
                if (!value.isTextual()) fail("Invalid string: " + name);
                if (!column.nullable() && value.asText().trim().isEmpty()) fail("Empty field: " + name);
                if (!field.isAnnotationPresent(javax.persistence.Lob.class) && !column.columnDefinition().equals("TEXT")
                        && value.asText().length() > column.length()) fail("Field too long: " + name);
            } else if (type == boolean.class || type == Boolean.class) {
                if (!value.isBoolean()) fail("Invalid boolean: " + name);
            } else if (type == Long.class || type == Integer.class) {
                if (!value.isIntegralNumber() || !value.canConvertToLong()
                        || (type == Integer.class && !value.canConvertToInt())) fail("Invalid number: " + name);
                if (value.longValue() < 0) fail("Negative number: " + name);
            } else if (type.isEnum() || type == LocalDateTime.class) {
                if (!value.isTextual()) fail("Invalid value: " + name);
            }
            try { row.set(name, mapper.valueToTree(mapper.convertValue(value, type))); }
            catch (IllegalArgumentException ex) { fail("Invalid value: " + section.name + "." + name); }
        }
        row.fieldNames().forEachRemaining(name -> { if (!fields.contains(name)) fail("Unknown field: " + section.name + "." + name); });
        if (section.name.equals("websiteSettings") && row.path("id").longValue() != 1) fail("Invalid website settings ID");
        return row;
    }

    private void fill(Section section, Object entity, ObjectNode row) {
        try {
            for (Field field : section.type.getDeclaredFields()) {
                if (field.getAnnotation(Column.class) == null) continue;
                field.setAccessible(true);
                field.set(entity, mapper.convertValue(row.get(field.getName()), field.getType()));
            }
        } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
    }

    private void setId(Section section, Object entity, Long id) {
        try {
            Field field = section.type.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private Object instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private ObjectNode content(ObjectNode row) {
        ObjectNode value = row.deepCopy();
        value.remove("id");
        return value;
    }

    private String key(Section section, ObjectNode row) {
        ArrayNode key = mapper.createArrayNode();
        for (String field : section.keys) {
            JsonNode value = row.get(field);
            if (value == null || value.isNull() || value.asText().trim().isEmpty()) fail("Missing business key: " + section.name);
            key.add(value);
        }
        return key.toString();
    }

    private void remap(ObjectNode row, String field, Map<Long, Long> ids, boolean nullable) {
        if (nullable && row.path(field).isNull()) return;
        Long target = ids == null ? null : ids.get(row.path(field).longValue());
        if (target == null) fail("Missing referenced record: " + field);
        row.put(field, target);
    }

    private void checkReferenceApp(ObjectNode row, String field, Map<Long, String> apps) {
        String app = apps == null ? null : apps.get(row.path(field).longValue());
        if (app == null || !app.equals(row.path("appId").asText())) fail("Invalid app for reference: " + field);
    }

    private void remapMedia(ObjectNode row, String field, Map<Long, Long> ids) {
        if (!row.hasNonNull(field) || row.path(field).asText().trim().isEmpty()) return;
        try {
            JsonNode assets = mapper.readTree(row.path(field).asText());
            if (!assets.isArray()) fail("Invalid media list: " + field);
            boolean changed = false;
            for (JsonNode asset : assets) {
                if (!asset.isObject()) fail("Invalid media record: " + field);
                if (!asset.hasNonNull("id")) continue;
                if (!asset.path("id").isIntegralNumber() || !asset.path("id").canConvertToLong()) fail("Invalid media ID: " + field);
                long source = asset.path("id").longValue();
                Long target = ids == null ? null : ids.get(source);
                if (target == null) fail("Missing referenced media: " + field);
                if (source != target) { ((ObjectNode) asset).put("id", target); changed = true; }
            }
            if (changed) row.put(field, mapper.writeValueAsString(assets));
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) { fail("Invalid media JSON: " + field); }
    }

    private static void fail(String message) { throw new BusinessException(ErrorCode.BAD_REQUEST, message); }

    private static class Section {
        final String name;
        final Class<?> type;
        final String[] keys;
        Section(String name, Class<?> type, String... keys) { this.name = name; this.type = type; this.keys = keys; }
    }
}
