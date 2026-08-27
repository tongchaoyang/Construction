package com.landmarkconstruction.backend.dto.quote;

import jakarta.validation.constraints.NotBlank;

public record QuoteStatusUpdateRequest(
        @NotBlank String status,
        String adminNotes
) {
}
