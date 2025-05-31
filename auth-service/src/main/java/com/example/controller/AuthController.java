package com.example.controller;

import com.example.dto.JwtResponse;
import com.example.dto.LoginRequest;
import com.example.dto.UserRegistrationRequest;
import com.example.dto.UserResponse;
import com.example.model.User;
import com.example.security.JwtUtil;
import com.example.service.AuthService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Authentication Controller for JWT token generation and user management.
 * Handles authentication, registration, and user profile operations.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // TODO: Configure proper CORS in Phase 2
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms:86400000}")
    private Long jwtExpirationMs;

    /**
     * Authenticates user and generates JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Validate user credentials
            boolean isValid = authService.validateUser(loginRequest.getUsername(), loginRequest.getPassword());
            
            if (!isValid) {
                return ResponseEntity.badRequest()
                    .body("Error: Invalid username or password!");
            }

            // Get user roles (for now, default to USER role)
            String roles = authService.getUserRoles(loginRequest.getUsername());

            // Generate JWT token
            String jwt = jwtUtil.generateToken(loginRequest.getUsername(), roles, null);

            return ResponseEntity.ok(new JwtResponse(
                jwt,
                loginRequest.getUsername(),
                roles,
                jwtExpirationMs / 1000 // Convert to seconds
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error: Authentication failed - " + e.getMessage());
        }
    }

    /**
     * Validates JWT token.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "message", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "message", "Token validation failed"));
        }
    }

    /**
     * Refresh JWT token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {        try {
            String token = request.get("token");
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);
                
                String newToken = jwtUtil.generateToken(username, roles, null);
                
                return ResponseEntity.ok(new JwtResponse(
                    newToken,
                    username,
                    roles,
                    jwtExpirationMs / 1000 // Convert to seconds
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token for refresh"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Token refresh failed"));
        }
    }    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    
                    // Try to get full user details from database
                    try {
                        User user = userService.findByUsername(username)
                            .orElse(userService.findByEmail(username).orElse(null));
                        
                        if (user != null) {
                            // Return full user profile from database
                            return ResponseEntity.ok(UserResponse.fromUser(user));
                        }
                    } catch (Exception e) {
                        // Continue to fallback
                    }
                    
                    // Fallback to token-based info for backward compatibility
                    String roles = jwtUtil.extractRoles(token);
                    return ResponseEntity.ok(Map.of(
                        "username", username,
                        "roles", roles,
                        "authenticated", true,
                        "source", "token" // Indicates this is fallback data
                    ));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false, "message", "Invalid or missing token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false, "message", "Authentication failed"));
        }
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest signUpRequest) {
        try {
            // Create new user account
            User user = userService.registerUser(
                signUpRequest.getUsername(), 
                signUpRequest.getEmail(), 
                signUpRequest.getPassword(),
                signUpRequest.getRole()
            );

            // Return user data (without password)
            UserResponse userResponse = UserResponse.fromUser(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully!",
                "user", userResponse
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check username availability
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of(
            "username", username,
            "available", available
        ));
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of(
            "email", email,
            "available", available
        ));
    }

    // Inner classes for responses
    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String roles;
        private String message;

        public TokenValidationResponse(boolean valid, String username, String roles, String message) {
            this.valid = valid;
            this.username = username;
            this.roles = roles;
            this.message = message;
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRoles() { return roles; }
        public void setRoles(String roles) { this.roles = roles; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
