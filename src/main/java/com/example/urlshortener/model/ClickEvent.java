package com.example.urlshortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "click_events",
        indexes = @Index(name = "idx_click_events_short_code", columnList = "short_code")
)
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType deviceType;

    @Column(nullable = false)
    private LocalDateTime clickedAt = LocalDateTime.now();

    protected ClickEvent() {
        // JPA needs an empty constructor
    }

    public ClickEvent(String shortCode, String country, DeviceType deviceType) {
        this.shortCode = shortCode;
        this.country = country;
        this.deviceType = deviceType;
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getCountry() { return country; }
    public DeviceType getDeviceType() { return deviceType; }
    public LocalDateTime getClickedAt() { return clickedAt; }
}
