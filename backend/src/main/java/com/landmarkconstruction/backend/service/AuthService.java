package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.dto.auth.*;
import com.landmarkconstruction.backend.exception.BadRequestException;
import com.landmarkconstruction.backend.model.User;
import com.landmarkconstruction.backend.model.UserSsoIdentity;
import com.landmarkconstruction.backend.model.enums.Role;
import com.landmarkconstruction.backend.repository.UserRepository;
import com.landmarkconstruction.backend.repository.UserSsoIdentityRepository;
import com.landmarkconstruction.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSsoIdentityRepository ssoIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists.");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));
        if (user.getPasswordHash() == null) {
            throw new BadRequestException(
                    "This account uses Single Sign-On. Please use the \"Sign in with Google\" (or SSO) option instead of a password.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return buildAuthResponse(user);
    }

    /**
     * Called after a successful OIDC login. Links to an existing SSO identity if one exists;
     * otherwise auto-links by verified email to an existing account, or creates a new customer account.
     */
    @Transactional
    public AuthResponse processOidcLogin(String provider, String subject, String email, String fullName) {
        User user = ssoIdentityRepository.findByProviderAndSubject(provider, subject)
                .map(UserSsoIdentity::getUser)
                .orElseGet(() -> linkOrCreateUser(provider, subject, email, fullName));
        return buildAuthResponse(user);
    }

    private User linkOrCreateUser(String provider, String subject, String email, String fullName) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(normalizedEmail);
            newUser.setFullName(fullName != null && !fullName.isBlank() ? fullName : normalizedEmail);
            newUser.setRole(Role.CUSTOMER);
            newUser.setPasswordHash(null);
            return userRepository.save(newUser);
        });

        UserSsoIdentity identity = new UserSsoIdentity();
        identity.setUser(user);
        identity.setProvider(provider);
        identity.setSubject(subject);
        identity.setEmail(email);
        ssoIdentityRepository.save(identity);

        return user;
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        if (user.getPasswordHash() != null) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Current password is incorrect.");
            }
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public UserSummaryDto toSummary(User user) {
        return new UserSummaryDto(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(), user.getRole().name());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, toSummary(user));
    }
}
