package com.landmarkconstruction.backend.dto.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectDetailDto(
        Long id,
        String titleEn,
        String titleZh,
        String descriptionEn,
        String descriptionZh,
        String category,
        String location,
        LocalDate completedDate,
        String coverImageUrl,
        boolean published,
        List<ProjectImageDto> images,
        double averageRating,
        long reviewCount
) {
}
