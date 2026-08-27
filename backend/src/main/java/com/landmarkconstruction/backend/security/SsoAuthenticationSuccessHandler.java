package com.landmarkconstruction.backend.security;

import com.landmarkconstruction.backend.dto.auth.AuthResponse;
import com.landmarkconstruction.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class SsoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)
                || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendRedirect("/login.html?error=sso_failed");
            return;
        }

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String fullName = oidcUser.getFullName();

        if (email == null || email.isBlank()) {
            log.warn("SSO login via {} did not return an email claim; cannot link/create an account.", provider);
            response.sendRedirect("/login.html?error=sso_no_email");
            return;
        }

        AuthResponse authResponse = authService.processOidcLogin(provider, subject, email, fullName);
        String encodedToken = URLEncoder.encode(authResponse.token(), StandardCharsets.UTF_8);
        response.sendRedirect("/sso-callback.html#token=" + encodedToken);
    }
}
