package com.example.service;

import org.springframework.stereotype.Service;

/**
 * Authentication service for user validation.
 * For now, uses hardcoded users for demonstration.
 * TODO: Integrate with database and proper password hashing in Phase 2.
 */
@Service
public class AuthService {

    /**
     * Validates user credentials.
     * For demo purposes, uses hardcoded users.
     * 
     * @param username Username
     * @param password Password
     * @return true if credentials are valid
     */
    public boolean validateUser(String username, String password) {
        // TODO: Replace with database lookup and proper password hashing
        // For now, using demo users for testing
        return ("admin".equals(username) && "admin123".equals(password)) ||
               ("user".equals(username) && "user123".equals(password)) ||
               ("test".equals(username) && "test123".equals(password));
    }

    /**
     * Gets user roles.
     * 
     * @param username Username
     * @return User roles (comma-separated)
     */
    public String getUserRoles(String username) {
        // TODO: Replace with database lookup
        // For now, simple role assignment
        if ("admin".equals(username)) {
            return "ADMIN,USER";
        } else {
            return "USER";
        }
    }

    /**
     * Checks if user exists.
     * 
     * @param username Username
     * @return true if user exists
     */
    public boolean userExists(String username) {
        // TODO: Replace with database lookup
        return "admin".equals(username) || 
               "user".equals(username) || 
               "test".equals(username);
    }
}
