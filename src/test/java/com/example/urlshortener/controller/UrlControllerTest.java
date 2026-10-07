package com.example.urlshortener.controller;

import com.example.urlshortener.config.WebConfig;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.ratelimit.RateLimitResult;
import com.example.urlshortener.ratelimit.RateLimiterService;
import com.example.urlshortener.service.ClickTrackingService;
import com.example.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@Import(WebConfig.class) // makes sure our rate-limit "bouncers" are standing at the door
class UrlControllerTest {

    private static final String LONG_URL = "https://www.github.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private ClickTrackingService clickTrackingService;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void letEveryoneIn() {
        when(rateLimiterService.check(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitResult(true, 10, 9, 0));
    }

    @Test
    void shorten_returns201AndTheShortUrl() throws Exception {
        when(urlService.shorten(LONG_URL)).thenReturn(new UrlMapping("abc123", LONG_URL));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"" + LONG_URL + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc123"));
    }

    @Test
    void shorten_returns400ForAnInvalidUrl() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"hello\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(urlService);
    }

    @Test
    void redirect_returns302AndTracksTheClick() throws Exception {
        when(urlService.getOriginalUrl("abc123")).thenReturn(LONG_URL);

        mockMvc.perform(get("/abc123")
                        .header("User-Agent", "TestBrowser")
                        .header("CF-IPCountry", "IN"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", LONG_URL));

        verify(clickTrackingService).trackClick("abc123", "TestBrowser", "IN");
    }

    @Test
    void redirect_returns404ForAnUnknownCode() throws Exception {
        when(urlService.getOriginalUrl("nope")).thenThrow(new UrlNotFoundException("nope"));

        mockMvc.perform(get("/nope"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(clickTrackingService);
    }

    @Test
    void shorten_returns429WhenRateLimitIsExceeded() throws Exception {
        when(rateLimiterService.check(eq("shorten"), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitResult(false, 10, 0, 42));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"" + LONG_URL + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "42"));

        verifyNoInteractions(urlService);
    }
}
