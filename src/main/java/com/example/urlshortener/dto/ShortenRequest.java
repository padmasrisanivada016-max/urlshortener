package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
        @NotBlank(message = "url must not be empty")
        @URL(message = "url must be a valid link")
        String url
) {
}
