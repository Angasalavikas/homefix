package com.homefix.authservice.seed;

import com.homefix.authservice.entity.Role;
import com.homefix.authservice.entity.User;
import com.homefix.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps a default ADMIN account on startup so the admin dashboard is reachable.
 *
 * Credentials come from environment variables (never committed):
 *   ADMIN_EMAIL    (default: admin@homefix.com — dev only)
 *   ADMIN_PASSWORD (default: Admin@1234 — dev only, change in production)
 *
 * Registration deliberately cannot self-assign the ADMIN role (see AuthService),
 * so this seeder is the supported way to create the first admin.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@homefix.com}")
    private String adminEmail;

    @Value("${admin.password:Admin@1234}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .fullName("HomeFix Admin")
                .email(adminEmail.toLowerCase().trim())
                .phone("+0000000000")
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Seeded default ADMIN account: {} (CHANGE THE DEFAULT PASSWORD IN PRODUCTION!)",
                adminEmail);
    }
}
