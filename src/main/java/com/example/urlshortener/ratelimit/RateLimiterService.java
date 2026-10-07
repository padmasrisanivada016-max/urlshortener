package com.example.urlshortener.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final StringRedisTemplate redis;

    public RateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public RateLimitResult check(String ruleName, String clientId, int limit, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long windowNumber = now / windowSeconds;
        long secondsLeftInWindow = windowSeconds - (now % windowSeconds);

        String key = "rate:" + ruleName + ":" + clientId + ":" + windowNumber;

        try {
            Long count = redis.opsForValue().increment(key);

            if (count != null && count == 1) {
                redis.expire(key, Duration.ofSeconds(windowSeconds));
            }

            long used = (count == null) ? 0 : count;
            boolean allowed = used <= limit;
            long remaining = Math.max(0, limit - used);

            return new RateLimitResult(allowed, limit, remaining, secondsLeftInWindow);

        } catch (RuntimeException e) {
            // If Redis is down, we let people in. Better a little unprotected than a dead app.
            log.warn("Rate limiter could not reach Redis, allowing the request", e);
            return new RateLimitResult(true, limit, limit, 0);
        }
    }
}
