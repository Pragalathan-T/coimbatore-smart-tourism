package com.yourorg.tourism.booking.dto;

import java.time.Instant;
import java.util.UUID;

import com.yourorg.tourism.booking.entity.BookingStatus;

public record BookingResponseDto(
        UUID id,
        UUID touristId,
        UUID guideId,
        UUID placeId,
        Instant scheduledAt,
        BookingStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
