package com.landmarkconstruction.backend.config;

import com.landmarkconstruction.backend.security.JwtAuthenticationFilter;
import com.landmarkconstruction.backend.security.SsoAuthenticationFailureHandler;
import com.landmarkconstruction.backend.security.SsoAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SsoAuthenticationSuccessHandler ssoAuthenticationSuccessHandler;
    private final SsoAuthenticationFailureHandler ssoAuthenticationFailureHandler;
    private final DaoAuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // OAuth2/OIDC login needs a short-lived session to hold state/nonce during the
                // redirect round-trip, so we can't stay fully SessionCreationPolicy.STATELESS.
                // But Spring's default session-backed SecurityContextRepository would then persist
                // whatever JwtAuthenticationFilter authenticates into that session and silently
                // restore it on later requests via the session cookie — regardless of the JWT sent,
                // even after logout. NullSecurityContextRepository disables that persistence so the
                // JWT (checked fresh per request) remains the only source of authentication; the
                // OAuth2 authorization-request tracking used during login uses a separate mechanism
                // and is unaffected.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(new NullSecurityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/sso-providers").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/quote-requests").permitAll()
                        .requestMatchers("GET", "/api/projects/**").permitAll()
                        .requestMatchers("GET", "/api/projects/*/reviews").permitAll()
                        .requestMatchers("/", "/*.html", "/admin/*.html", "/assets/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Without this, adding oauth2Login() makes Spring Security's default entry point
                // redirect any unauthenticated request to its auto-generated /login page (a 302 to
                // a 200 OK HTML page) instead of returning 401 — which fetch() follows silently,
                // breaking every API call made without a valid JWT. Our own login pages and the
                // explicit /oauth2/authorization/{id} links are unaffected by this.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(ssoAuthenticationSuccessHandler)
                        .failureHandler(ssoAuthenticationFailureHandler));
        return http.build();
    }
}
