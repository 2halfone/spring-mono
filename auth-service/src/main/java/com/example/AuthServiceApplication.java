package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Application Class for Auth Service
 * 
 * This microservice handles:
 * - User authentication and JWT token generation
 * - User registration and management
 * - Security validation for other microservices
 * 
 * JWT Security Features:
 * - Stateless authentication
 * - Token generation and validation
 * - Role-based authorization
 * - Environment-based configuration
 */
@SpringBootApplication
@EnableJpaAuditing
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
