package com.example.config;

import com.example.model.Role;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database initializer - Creates default users if they don't exist.
 * 
 * Creates:
 * - admin / admin123 (ADMIN role)
 * - user / user123 (USER role)  
 * - test / test123 (USER role)
 * 
 * This ensures backward compatibility with existing hardcoded users
 * while providing database persistence.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        try {
            createDefaultUsers();
            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage(), e);
        }
    }

    private void createDefaultUsers() {
        // Create admin user if not exists
        if (userService.isUsernameAvailable("admin")) {
            try {
                userService.registerUser("admin", "admin@example.com", "admin123", Role.ADMIN);
                logger.info("Created default admin user: admin / admin123");
            } catch (Exception e) {
                logger.warn("Could not create admin user: " + e.getMessage());
            }
        } else {
            logger.info("Admin user already exists, skipping creation");
        }

        // Create regular user if not exists
        if (userService.isUsernameAvailable("user")) {
            try {
                userService.registerUser("user", "user@example.com", "user123", Role.USER);
                logger.info("Created default user: user / user123");
            } catch (Exception e) {
                logger.warn("Could not create user: " + e.getMessage());
            }
        } else {
            logger.info("User 'user' already exists, skipping creation");
        }

        // Create test user if not exists
        if (userService.isUsernameAvailable("test")) {
            try {
                userService.registerUser("test", "test@example.com", "test123", Role.USER);
                logger.info("Created default test user: test / test123");
            } catch (Exception e) {
                logger.warn("Could not create test user: " + e.getMessage());
            }
        } else {
            logger.info("Test user already exists, skipping creation");
        }

        // Log total user count
        long totalUsers = userService.getTotalUserCount();
        logger.info("Total users in database: {}", totalUsers);
    }
}
