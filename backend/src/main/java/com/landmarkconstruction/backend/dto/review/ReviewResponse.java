package com.landmarkconstruction.backend.dto.review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long projectId,
        Long userId,
        String userFullName,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
