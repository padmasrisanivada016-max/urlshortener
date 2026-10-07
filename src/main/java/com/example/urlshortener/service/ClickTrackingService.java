package com.example.urlshortener.service;

import com.example.urlshortener.model.ClickEvent;
import com.example.urlshortener.repository.ClickEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ClickTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingService.class);
    private static final String UNKNOWN_COUNTRY = "UNKNOWN";

    private final ClickEventRepository clickEventRepository;
    private final DeviceDetector deviceDetector;

    public ClickTrackingService(ClickEventRepository clickEventRepository,
                                DeviceDetector deviceDetector) {
        this.clickEventRepository = clickEventRepository;
        this.deviceDetector = deviceDetector;
    }

    @Async("clickExecutor")
    public void trackClick(String shortCode, String userAgent, String countryHeader) {
        try {
            ClickEvent click = new ClickEvent(
                    shortCode,
                    resolveCountry(countryHeader),
                    deviceDetector.detect(userAgent)
            );
            clickEventRepository.save(click);
        } catch (Exception e) {
            // A tracking problem must never break the user's redirect
            log.warn("Could not save click for {}", shortCode, e);
        }
    }

    private String resolveCountry(String countryHeader) {
        if (countryHeader == null || countryHeader.isBlank()) {
            return UNKNOWN_COUNTRY;
        }
        return countryHeader.trim().toUpperCase();
    }
}
