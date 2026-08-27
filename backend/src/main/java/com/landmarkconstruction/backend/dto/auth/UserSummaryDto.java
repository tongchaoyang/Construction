package com.landmarkconstruction.backend.dto.auth;

public record UserSummaryDto(
        Long id,
        String email,
        String fullName,
        String phone,
        String role
) {
}
