package com.landmarkconstruction.backend.dto.auth;

public record SsoProvidersResponse(
        boolean google,
        boolean oidc,
        String oidcDisplayName
) {
}
