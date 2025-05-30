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

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
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
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);
                
                String newToken = jwtUtil.generateToken(username, roles);
                
                return ResponseEntity.ok(new JwtResponse(
                    newToken,
                    "Bearer",
                    username,
                    String.join(",", roles),
                    jwtUtil.getExpirationFromToken(newToken).getTime() - System.currentTimeMillis()
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token for refresh"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Token refresh failed"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    List<String> roles = jwtUtil.getRolesFromToken(token);
                    
                    return ResponseEntity.ok(Map.of(
                        "username", username,
                        "roles", roles,
                        "authenticated", true
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
