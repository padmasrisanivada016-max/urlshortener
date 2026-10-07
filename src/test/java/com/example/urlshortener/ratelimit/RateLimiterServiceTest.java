package com.example.urlshortener.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiter;

    @Test
    void allowsRequestsUnderTheLimit() {
        whenRedisCounterIs(3L);

        RateLimitResult result = rateLimiter.check("shorten", "1.2.3.4", LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(7);
    }

    @Test
    void blocksRequestsOverTheLimit() {
        whenRedisCounterIs(11L);

        RateLimitResult result = rateLimiter.check("shorten", "1.2.3.4", LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isFalse();
        assertThat(result.remaining()).isZero();
    }

    @Test
    void setsExpiryOnTheFirstRequestOfAWindow() {
        whenRedisCounterIs(1L);

        rateLimiter.check("shorten", "1.2.3.4", LIMIT, WINDOW_SECONDS);

        verify(redis).expire(anyString(), eq(Duration.ofSeconds(WINDOW_SECONDS)));
    }

    @Test
    void doesNotResetExpiryOnLaterRequests() {
        whenRedisCounterIs(2L);

        rateLimiter.check("shorten", "1.2.3.4", LIMIT, WINDOW_SECONDS);

        verify(redis, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void failsOpenWhenRedisIsDown() {
        when(redis.opsForValue()).thenThrow(new RuntimeException("Redis is down"));

        RateLimitResult result = rateLimiter.check("shorten", "1.2.3.4", LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
    }

    private void whenRedisCounterIs(long count) {
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(count);
    }
}
