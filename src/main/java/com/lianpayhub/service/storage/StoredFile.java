package com.lianpayhub.service.storage;

public class StoredFile {
    private final String key;
    private final long size;
    private final String contentType;

    public StoredFile(String key, long size, String contentType) {
        this.key = key;
        this.size = size;
        this.contentType = contentType;
    }

    public String getKey() { return key; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
}
