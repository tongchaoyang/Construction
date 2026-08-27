package com.landmarkconstruction.backend.dto.project;

public record ProjectImageDto(
        Long id,
        String imageUrl,
        int sortOrder
) {
}
