package com.landmarkconstruction.backend.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Minimal registration-id -&gt; ClientRegistration lookup that, unlike
 * InMemoryClientRegistrationRepository, tolerates being empty (no SSO providers configured).
 */
public class SimpleClientRegistrationRepository implements ClientRegistrationRepository {

    private final Map<String, ClientRegistration> registrations;

    public SimpleClientRegistrationRepository(List<ClientRegistration> registrations) {
        this.registrations = registrations.stream()
                .collect(Collectors.toMap(ClientRegistration::getRegistrationId, Function.identity()));
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.get(registrationId);
    }

    public boolean hasRegistration(String registrationId) {
        return registrations.containsKey(registrationId);
    }
}
