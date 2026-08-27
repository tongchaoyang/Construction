package com.landmarkconstruction.backend.dto.quote;

import java.time.Instant;
import java.util.List;

public record QuoteRequestResponse(
        Long id,
        String name,
        String email,
        String phone,
        String category,
        String description,
        String status,
        String adminNotes,
        Instant createdAt,
        List<String> imageUrls
) {
}
