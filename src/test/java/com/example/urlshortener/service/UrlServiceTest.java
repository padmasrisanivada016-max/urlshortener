package com.example.urlshortener.service;

import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    private static final String LONG_URL = "https://www.github.com";

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlService urlService;

    @Test
    void shorten_savesLinkWithSixCharacterCode() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(call -> call.getArgument(0));

        UrlMapping result = urlService.shorten(LONG_URL);

        assertThat(result.getOriginalUrl()).isEqualTo(LONG_URL);
        assertThat(result.getShortCode()).hasSize(6).matches("[A-Za-z0-9]+");
    }

    @Test
    void shorten_triesAgainWhenCodeIsAlreadyTaken() {
        // 1st code is taken, 2nd code is free
        when(repository.existsByShortCode(anyString())).thenReturn(true, false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(call -> call.getArgument(0));

        urlService.shorten(LONG_URL);

        verify(repository, times(2)).existsByShortCode(anyString());
    }

    @Test
    void getOriginalUrl_returnsTheLongUrl() {
        when(repository.findByShortCode("abc123"))
                .thenReturn(Optional.of(new UrlMapping("abc123", LONG_URL)));

        String result = urlService.getOriginalUrl("abc123");

        assertThat(result).isEqualTo(LONG_URL);
    }

    @Test
    void getOriginalUrl_throwsWhenCodeDoesNotExist() {
        when(repository.findByShortCode("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getOriginalUrl("nope"))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessageContaining("nope");
    }
}
