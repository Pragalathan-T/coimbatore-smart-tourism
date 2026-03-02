package com.yourorg.tourism.place.dto;

import java.time.Instant;
import java.util.UUID;

public record PlaceResponseDto(
        UUID id,
        String title,
        String description,
        String address,
        String mapUrl,
        Instant createdAt
) {
}
