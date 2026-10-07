package com.example.urlshortener.service;

import com.example.urlshortener.model.ClickEvent;
import com.example.urlshortener.model.DeviceType;
import com.example.urlshortener.repository.ClickEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickTrackingServiceTest {

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private DeviceDetector deviceDetector;

    @InjectMocks
    private ClickTrackingService clickTrackingService;

    @Captor
    private ArgumentCaptor<ClickEvent> clickCaptor;

    @Test
    void trackClick_savesCountryInUpperCaseAndDevice() {
        when(deviceDetector.detect("some-user-agent")).thenReturn(DeviceType.MOBILE);

        clickTrackingService.trackClick("abc123", "some-user-agent", " in ");

        verify(clickEventRepository).save(clickCaptor.capture());
        ClickEvent saved = clickCaptor.getValue();
        assertThat(saved.getShortCode()).isEqualTo("abc123");
        assertThat(saved.getCountry()).isEqualTo("IN");
        assertThat(saved.getDeviceType()).isEqualTo(DeviceType.MOBILE);
    }

    @Test
    void trackClick_usesUnknownWhenCountryHeaderIsMissing() {
        when(deviceDetector.detect("some-user-agent")).thenReturn(DeviceType.DESKTOP);

        clickTrackingService.trackClick("abc123", "some-user-agent", null);

        verify(clickEventRepository).save(clickCaptor.capture());
        assertThat(clickCaptor.getValue().getCountry()).isEqualTo("UNKNOWN");
    }

    @Test
    void trackClick_doesNotThrowWhenDatabaseFails() {
        when(deviceDetector.detect("some-user-agent")).thenReturn(DeviceType.DESKTOP);
        when(clickEventRepository.save(any(ClickEvent.class)))
                .thenThrow(new RuntimeException("Database is down"));

        assertThatCode(() -> clickTrackingService.trackClick("abc123", "some-user-agent", "IN"))
                .doesNotThrowAnyException();
    }
}
