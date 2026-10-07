package com.example.urlshortener.config;

import com.example.urlshortener.ratelimit.RateLimitInterceptor;
import com.example.urlshortener.ratelimit.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiterService rateLimiter;
    private final int windowSeconds;
    private final int shortenLimit;
    private final int redirectLimit;

    public WebConfig(RateLimiterService rateLimiter,
                     @Value("${rate-limit.window-seconds}") int windowSeconds,
                     @Value("${rate-limit.shorten-limit}") int shortenLimit,
                     @Value("${rate-limit.redirect-limit}") int redirectLimit) {
        this.rateLimiter = rateLimiter;
        this.windowSeconds = windowSeconds;
        this.shortenLimit = shortenLimit;
        this.redirectLimit = redirectLimit;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new RateLimitInterceptor(rateLimiter, "shorten", shortenLimit, windowSeconds))
                .addPathPatterns("/api/shorten");

        registry.addInterceptor(
                        new RateLimitInterceptor(rateLimiter, "redirect", redirectLimit, windowSeconds))
                .addPathPatterns("/*")
                .excludePathPatterns("/error", "/favicon.ico");
    }
}
