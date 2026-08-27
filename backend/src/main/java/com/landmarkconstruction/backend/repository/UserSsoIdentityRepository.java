package com.landmarkconstruction.backend.repository;

import com.landmarkconstruction.backend.model.UserSsoIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSsoIdentityRepository extends JpaRepository<UserSsoIdentity, Long> {
    Optional<UserSsoIdentity> findByProviderAndSubject(String provider, String subject);
}
