package com.landmarkconstruction.backend.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectCreateUpdateRequest(
        @NotBlank String titleEn,
        @NotBlank String titleZh,
        @NotBlank String descriptionEn,
        @NotBlank String descriptionZh,
        @NotNull String category,
        String location,
        LocalDate completedDate,
        Boolean published
) {
}
