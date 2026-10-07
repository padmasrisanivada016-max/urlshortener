package com.example.urlshortener.service;

import com.example.urlshortener.dto.AnalyticsResponse;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.ClickEventRepository;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(UrlMappingRepository urlMappingRepository,
                            ClickEventRepository clickEventRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.clickEventRepository = clickEventRepository;
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        if (!urlMappingRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }

        return new AnalyticsResponse(
                shortCode,
                clickEventRepository.countByShortCode(shortCode),
                clickEventRepository.countByCountry(shortCode),
                clickEventRepository.countByDevice(shortCode),
                clickEventRepository.countByDay(shortCode)
        );
    }
}
