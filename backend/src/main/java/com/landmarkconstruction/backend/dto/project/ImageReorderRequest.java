package com.landmarkconstruction.backend.dto.project;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImageReorderRequest(
        @NotEmpty List<Long> imageIds
) {
}
