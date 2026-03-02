package com.yourorg.tourism.place.dto;

import jakarta.validation.constraints.Size;

public record UpdatePlaceRequestDto(
        @Size(max = 200) String title,
        String description,
        String address,
        String mapUrl
) {
}
