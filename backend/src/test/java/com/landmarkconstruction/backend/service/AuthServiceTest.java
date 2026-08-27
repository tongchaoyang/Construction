package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.dto.auth.AuthResponse;
import com.landmarkconstruction.backend.model.User;
import com.landmarkconstruction.backend.model.UserSsoIdentity;
import com.landmarkconstruction.backend.model.enums.Role;
import com.landmarkconstruction.backend.repository.UserRepository;
import com.landmarkconstruction.backend.repository.UserSsoIdentityRepository;
import com.landmarkconstruction.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSsoIdentityRepository ssoIdentityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService() {
        return new AuthService(userRepository, ssoIdentityRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void processOidcLogin_existingSsoIdentity_reusesLinkedUserWithoutCreatingAnything() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("jane@example.com");
        existingUser.setFullName("Jane Customer");
        existingUser.setRole(Role.CUSTOMER);

        UserSsoIdentity identity = new UserSsoIdentity();
        identity.setUser(existingUser);

        when(ssoIdentityRepository.findByProviderAndSubject("google", "sub-123")).thenReturn(Optional.of(identity));
        when(jwtService.generateToken(1L, "jane@example.com", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse response = authService().processOidcLogin("google", "sub-123", "jane@example.com", "Jane Customer");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("jane@example.com");
        verify(userRepository, never()).save(any());
        verify(ssoIdentityRepository, never()).save(any());
    }

    @Test
    void processOidcLogin_noExistingIdentityButEmailMatchesAccount_autoLinksByEmail() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");
        existingUser.setFullName("Existing User");
        existingUser.setRole(Role.CUSTOMER);
        existingUser.setPasswordHash("some-bcrypt-hash");

        when(ssoIdentityRepository.findByProviderAndSubject("google", "sub-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(2L, "existing@example.com", "CUSTOMER")).thenReturn("jwt-token-2");

        AuthResponse response = authService().processOidcLogin("google", "sub-456", "existing@example.com", "Existing User");

        assertThat(response.token()).isEqualTo("jwt-token-2");
        verify(userRepository, never()).save(any());

        ArgumentCaptor<UserSsoIdentity> identityCaptor = ArgumentCaptor.forClass(UserSsoIdentity.class);
        verify(ssoIdentityRepository).save(identityCaptor.capture());
        assertThat(identityCaptor.getValue().getUser()).isEqualTo(existingUser);
        assertThat(identityCaptor.getValue().getProvider()).isEqualTo("google");
        assertThat(identityCaptor.getValue().getSubject()).isEqualTo("sub-456");
    }

    @Test
    void processOidcLogin_noExistingUserAtAll_createsNewCustomerAccountWithNoPassword() {
        when(ssoIdentityRepository.findByProviderAndSubject("google", "sub-789")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newperson@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(jwtService.generateToken(3L, "newperson@example.com", "CUSTOMER")).thenReturn("jwt-token-3");

        AuthResponse response = authService().processOidcLogin("google", "sub-789", "newperson@example.com", "New Person");

        assertThat(response.token()).isEqualTo("jwt-token-3");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User created = userCaptor.getValue();
        assertThat(created.getEmail()).isEqualTo("newperson@example.com");
        assertThat(created.getFullName()).isEqualTo("New Person");
        assertThat(created.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(created.getPasswordHash()).isNull();

        verify(ssoIdentityRepository).save(any(UserSsoIdentity.class));
    }

    @Test
    void login_accountWithNullPasswordHash_rejectsWithSsoGuidanceInsteadOfCrashing() {
        User ssoOnlyUser = new User();
        ssoOnlyUser.setEmail("sso-only@example.com");
        ssoOnlyUser.setPasswordHash(null);

        when(userRepository.findByEmail("sso-only@example.com")).thenReturn(Optional.of(ssoOnlyUser));

        var request = new com.landmarkconstruction.backend.dto.auth.LoginRequest("sso-only@example.com", "whatever");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.landmarkconstruction.backend.exception.BadRequestException.class,
                () -> authService().login(request));

        verifyNoInteractions(authenticationManager);
    }
}
