package com.example.urlshortener.service;

import com.example.urlshortener.model.DeviceType;
import org.springframework.stereotype.Component;

@Component
public class DeviceDetector {

    public DeviceType detect(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return DeviceType.UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        if (isTablet(ua)) {
            return DeviceType.TABLET;
        }
        if (isMobile(ua)) {
            return DeviceType.MOBILE;
        }
        return DeviceType.DESKTOP;
    }

    private boolean isTablet(String ua) {
        boolean androidTablet = ua.contains("android") && !ua.contains("mobile");
        return ua.contains("ipad") || ua.contains("tablet") || androidTablet;
    }

    private boolean isMobile(String ua) {
        return ua.contains("mobi") || ua.contains("iphone") || ua.contains("android");
    }
}
