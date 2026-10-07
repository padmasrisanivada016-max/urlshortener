package com.example.urlshortener.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiter;
    private final String ruleName;
    private final int limit;
    private final int windowSeconds;

    public RateLimitInterceptor(RateLimiterService rateLimiter,
                                String ruleName,
                                int limit,
                                int windowSeconds) {
        this.rateLimiter = rateLimiter;
        this.ruleName = ruleName;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String clientId = request.getRemoteAddr();
        RateLimitResult result = rateLimiter.check(ruleName, clientId, limit, windowSeconds);

        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (result.allowed()) {
            return true; // come in!
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"Too many requests. Try again in "
                        + result.retryAfterSeconds() + " seconds.\"}");

        return false; // stop here, do not reach the controller
    }
}
