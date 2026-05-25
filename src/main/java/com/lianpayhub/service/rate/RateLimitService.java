package com.lianpayhub.service.rate;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public void requireWithinLimit(String key, int maxRequests, Duration duration) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.expiredAt.isBefore(now)) {
                return new Window(1, now.plus(duration));
            }
            return new Window(current.count + 1, current.expiredAt);
        });
        if (window.count > maxRequests) {
            throw new BusinessException(ErrorCode.RATE_LIMITED);
        }
    }

    private static class Window {
        private final int count;
        private final Instant expiredAt;

        private Window(int count, Instant expiredAt) {
            this.count = count;
            this.expiredAt = expiredAt;
        }
    }
}
