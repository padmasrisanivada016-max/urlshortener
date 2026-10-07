package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.ClickTrackingService;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final ClickTrackingService clickTrackingService;
    private final String baseUrl;

    public UrlController(UrlService urlService,
                         ClickTrackingService clickTrackingService,
                         @Value("${app.base-url}") String baseUrl) {
        this.urlService = urlService;
        this.clickTrackingService = clickTrackingService;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        UrlMapping saved = urlService.shorten(request.url());
        ShortenResponse response = new ShortenResponse(baseUrl + saved.getShortCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode,
                                         HttpServletRequest request) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        clickTrackingService.trackClick(
                shortCode,
                request.getHeader("User-Agent"),
                request.getHeader("CF-IPCountry")
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
