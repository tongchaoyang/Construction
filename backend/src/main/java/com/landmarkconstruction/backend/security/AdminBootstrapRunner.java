package com.landmarkconstruction.backend.security;

import com.landmarkconstruction.backend.model.User;
import com.landmarkconstruction.backend.model.enums.Role;
import com.landmarkconstruction.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-email}")
    private String bootstrapEmail;

    @Value("${app.admin.bootstrap-password}")
    private String bootstrapPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (!StringUtils.hasText(bootstrapEmail) || !StringUtils.hasText(bootstrapPassword)) {
            log.warn("No admin account exists yet and ADMIN_EMAIL/ADMIN_PASSWORD are not set. " +
                    "Set these environment variables and restart to create the first admin account.");
            return;
        }
        User admin = new User();
        admin.setEmail(bootstrapEmail);
        admin.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        admin.setFullName("Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Created initial admin account for {}", bootstrapEmail);
    }
}
