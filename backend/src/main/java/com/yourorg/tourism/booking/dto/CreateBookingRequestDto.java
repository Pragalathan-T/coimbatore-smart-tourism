package com.yourorg.tourism.booking.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequestDto(
        @NotNull UUID guideId,
        UUID placeId,
        Instant scheduledAt
) {
}
