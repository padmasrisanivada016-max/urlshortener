package com.example.urlshortener.service;

import com.example.urlshortener.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDetectorTest {

    private final DeviceDetector detector = new DeviceDetector();

    @Test
    void iphoneIsMobile() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Mobile/15E148";

        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.MOBILE);
    }

    @Test
    void androidPhoneIsMobile() {
        String userAgent = "Mozilla/5.0 (Linux; Android 13; Pixel 7) Chrome/120.0 Mobile Safari/537.36";

        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.MOBILE);
    }

    @Test
    void ipadIsTablet() {
        String userAgent = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X)";

        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.TABLET);
    }

    @Test
    void androidWithoutMobileWordIsTablet() {
        String userAgent = "Mozilla/5.0 (Linux; Android 13; SM-X700) Chrome/120.0 Safari/537.36";

        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.TABLET);
    }

    @Test
    void windowsLaptopIsDesktop() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0";

        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.DESKTOP);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void missingUserAgentIsUnknown(String userAgent) {
        assertThat(detector.detect(userAgent)).isEqualTo(DeviceType.UNKNOWN);
    }
}
