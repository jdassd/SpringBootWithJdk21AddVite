package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final SiteService siteService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${APP_ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${APP_ADMIN_PASSWORD:Admin123!}")
    private String adminPassword;

    public AdminAccountInitializer(UserRepository userRepository, SiteService siteService) {
        this.userRepository = userRepository;
        this.siteService = siteService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByRole("ADMIN")) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminUsername + "@example.com");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setFailedAttempts(0);
            admin.setCreatedAt(Instant.now());
            userRepository.save(admin);
            log.info("Created default admin user: {}", adminUsername);
        }

        userRepository.findAll().forEach(siteService::ensureSiteForUser);
    }
}
