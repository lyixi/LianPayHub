package com.lianpayhub.service.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NonceCacheService {

    private final Map<String, Instant> nonceMap = new ConcurrentHashMap<>();

    public boolean markIfAbsent(String key, int ttlSeconds) {
        cleanup();
        Instant expireAt = Instant.now().plusSeconds(ttlSeconds);
        return nonceMap.putIfAbsent(key, expireAt) == null;
    }

    private void cleanup() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> iterator = nonceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            if (entry.getValue().isBefore(now)) {
                iterator.remove();
            }
        }
    }
}
