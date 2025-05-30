package com.example.controller;

import com.example.dto.JwtResponse;
import com.example.dto.LoginRequest;
import com.example.security.JwtUtil;
import com.example.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller for JWT token generation.
 * Only handles authentication logic - token generation and refresh.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // TODO: Configure proper CORS in Phase 2
public class AuthController {

    @Autowired
    private AuthService authService;

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
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);
                
                return ResponseEntity.ok().body(new TokenValidationResponse(
                    true, 
                    username, 
                    roles,
                    "Token is valid"
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(new TokenValidationResponse(
                        false, 
                        null, 
                        null,
                        "Token is invalid or expired"
                    ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TokenValidationResponse(
                    false, 
                    null, 
                    null,
                    "Token validation failed: " + e.getMessage()
                ));
        }
    }

    /**
     * Refresh JWT token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);
                
                // Generate new token
                String newJwt = jwtUtil.generateToken(username, roles, null);
                
                return ResponseEntity.ok(new JwtResponse(
                    newJwt,
                    username,
                    roles,
                    jwtExpirationMs / 1000
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body("Error: Invalid or expired token!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error: Token refresh failed - " + e.getMessage());
        }
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
