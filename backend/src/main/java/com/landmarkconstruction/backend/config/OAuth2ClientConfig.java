package com.landmarkconstruction.backend.config;

import com.landmarkconstruction.backend.security.SimpleClientRegistrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class OAuth2ClientConfig {

    @Bean
    public SimpleClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.sso.google.client-id:}") String googleClientId,
            @Value("${app.sso.google.client-secret:}") String googleClientSecret,
            @Value("${app.sso.oidc.issuer-uri:}") String oidcIssuerUri,
            @Value("${app.sso.oidc.client-id:}") String oidcClientId,
            @Value("${app.sso.oidc.client-secret:}") String oidcClientSecret) {

        List<ClientRegistration> registrations = new ArrayList<>();

        if (StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret)) {
            registrations.add(googleClientRegistration(googleClientId, googleClientSecret));
            log.info("Google SSO enabled");
        }

        if (StringUtils.hasText(oidcIssuerUri) && StringUtils.hasText(oidcClientId) && StringUtils.hasText(oidcClientSecret)) {
            try {
                ClientRegistration oidcRegistration = ClientRegistrations.fromIssuerLocation(oidcIssuerUri)
                        .registrationId("oidc")
                        .clientId(oidcClientId)
                        .clientSecret(oidcClientSecret)
                        .scope("openid", "profile", "email")
                        .build();
                registrations.add(oidcRegistration);
                log.info("Generic OIDC SSO enabled (issuer: {})", oidcIssuerUri);
            } catch (Exception e) {
                log.warn("Failed to configure generic OIDC provider from issuer '{}' — SSO via this " +
                        "provider will be disabled. Cause: {}", oidcIssuerUri, e.getMessage());
            }
        }

        if (registrations.isEmpty()) {
            log.info("No SSO providers configured (set GOOGLE_CLIENT_ID/SECRET or OIDC_ISSUER_URI/CLIENT_ID/SECRET to enable).");
        }

        return new SimpleClientRegistrationRepository(registrations);
    }

    /**
     * Google's OIDC endpoints, hardcoded rather than resolved via issuer discovery so that an
     * unreachable network at startup can never prevent the application from booting.
     */
    private ClientRegistration googleClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .clientName("Google")
                .build();
    }
}
