package com.lianpayhub.service.search;

import java.util.LinkedHashMap;
import java.util.Map;

public class UnifiedSearchResult {
    private final String type;
    private final Long id;
    private final String appId;
    private final String title;
    private final String summary;
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public UnifiedSearchResult(String type, Long id, String appId, String title, String summary) {
        this.type = type;
        this.id = id;
        this.appId = appId;
        this.title = title;
        this.summary = summary;
    }

    public UnifiedSearchResult attr(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
