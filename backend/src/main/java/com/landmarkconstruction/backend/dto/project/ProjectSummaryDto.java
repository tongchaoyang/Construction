package com.landmarkconstruction.backend.dto.project;

import java.time.LocalDate;

public record ProjectSummaryDto(
        Long id,
        String titleEn,
        String titleZh,
        String category,
        String location,
        LocalDate completedDate,
        String coverImageUrl,
        boolean published,
        double averageRating,
        long reviewCount
) {
}
