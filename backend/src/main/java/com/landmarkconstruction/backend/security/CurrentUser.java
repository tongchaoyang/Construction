package com.landmarkconstruction.backend.security;

import com.landmarkconstruction.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUser {

    public Optional<User> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal.getUser());
    }

    public User require() {
        return get().orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Authentication required"));
    }
}
