package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.auth.*;
import com.landmarkconstruction.backend.security.CurrentUser;
import com.landmarkconstruction.backend.security.SimpleClientRegistrationRepository;
import com.landmarkconstruction.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final SimpleClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.sso.oidc.display-name:SSO}")
    private String oidcDisplayName;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserSummaryDto me() {
        return authService.toSummary(currentUser.require());
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.require(), request);
    }

    @GetMapping("/sso-providers")
    public SsoProvidersResponse ssoProviders() {
        return new SsoProvidersResponse(
                clientRegistrationRepository.hasRegistration("google"),
                clientRegistrationRepository.hasRegistration("oidc"),
                oidcDisplayName);
    }
}
