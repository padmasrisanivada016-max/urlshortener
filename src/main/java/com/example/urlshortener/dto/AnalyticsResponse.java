package com.example.urlshortener.dto;

import java.util.List;

public record AnalyticsResponse(
        String shortCode,
        long totalClicks,
        List<CountStat> clicksByCountry,
        List<CountStat> clicksByDevice,
        List<CountStat> clicksByDay
) {
}
