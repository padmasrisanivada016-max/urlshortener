package com.example.urlshortener.ratelimit;

public record RateLimitResult(
        boolean allowed,
        int limit,
        long remaining,
        long retryAfterSeconds
) {
}
