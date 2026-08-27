package com.landmarkconstruction.backend.dto.auth;

public record AuthResponse(
        String token,
        UserSummaryDto user
) {
}
