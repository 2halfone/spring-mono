package com.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations in Gateway.
 * Only handles token validation - NO TOKEN GENERATION.
 * 
 * Security Features:
 * - Environment-based secret key (no hardcoded secrets)
 * - Stateless validation (no database calls)
 * - Signature verification
 * - Expiration validation
 * - Claims extraction
 */
@Component
public class JwtUtil {

    // Environment-based secret key - NEVER hardcode secrets
    @Value("${jwt.secret:#{null}}")
    private String jwtSecret;

    // Issuer for token validation
    @Value("${jwt.issuer:spring-microservices}")
    private String jwtIssuer;

    /**
     * Generates a secret key from the environment variable.
     * 
     * @return SecretKey for JWT validation
     * @throws IllegalStateException if JWT secret is not configured
     */
    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret not configured. Please set JWT_SECRET environment variable."
            );
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Validates a JWT token (stateless validation).
     * Performs signature verification, expiration check, and basic structure validation.
     * NO DATABASE CALLS - purely stateless validation.
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expired
            return false;
        } catch (UnsupportedJwtException e) {
            // Unsupported JWT token
            return false;
        } catch (MalformedJwtException e) {
            // Invalid JWT token format
            return false;
        } catch (SecurityException e) {
            // Invalid JWT signature
            return false;
        } catch (IllegalArgumentException e) {
            // JWT claims string is empty
            return false;
        }
    }

    /**
     * Extracts username from JWT token.
     * 
     * @param token JWT token
     * @return Username from token subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts roles from JWT token.
     * 
     * @param token JWT token
     * @return Roles string from token claims
     */
    public String extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", String.class));
    }

    /**
     * Extracts expiration date from JWT token.
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from JWT token.
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from JWT token.
     * 
     * @param token JWT token
     * @return All claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates token and checks if it belongs to the specified username.
     * 
     * @param token JWT token
     * @param username Username to validate against
     * @return true if token is valid and belongs to user
     */
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && validateToken(token));
    }

    /**
     * Extracts token from Authorization header.
     * Expected format: "Bearer <token>"
     * 
     * @param authHeader Authorization header value
     * @return JWT token or null if header is invalid
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
