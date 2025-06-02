package com.example.gateway;

import com.example.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter for Gateway.
 * 
 * SECURITY GATEWAY - CENTRALIZED JWT VALIDATION
 * This filter acts as the SINGLE POINT of authentication for all microservices.
 * 
 * Security Features:
 * - Centralized JWT validation (no database calls)
 * - Stateless authentication
 * - Claims forwarding to downstream services
 * - Security audit logging
 * - Proper HTTP status codes for auth failures
 * 
 * IMPORTANT: This is the ONLY place where JWT validation happens.
 * Individual microservices should trust the headers forwarded by this gateway.
 */
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String path = request.getPath().value();
            String method = request.getMethod().name();
            String clientIP = getClientIP(request);

            // Log all requests for security audit
            logger.info("Gateway Request: {} {} from {}", method, path, clientIP);

            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                securityLogger.info("PUBLIC ACCESS: {} {} from {} - No authentication required", 
                    method, path, clientIP);
                return chain.filter(exchange);
            }

            // PROTECTED ENDPOINT - JWT VALIDATION REQUIRED
            securityLogger.info("PROTECTED ACCESS: {} {} from {} - JWT validation required", 
                method, path, clientIP);

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = jwtUtil.extractTokenFromHeader(authHeader);

            if (token == null) {
                securityLogger.warn("AUTH FAILURE: Missing JWT token for {} {} from {}", 
                    method, path, clientIP);
                return unauthorizedResponse(response, "Missing or invalid Authorization header");
            }

            // Validate JWT token
            if (!jwtUtil.validateToken(token)) {
                securityLogger.warn("AUTH FAILURE: Invalid JWT token for {} {} from {}", 
                    method, path, clientIP);
                return unauthorizedResponse(response, "Invalid or expired JWT token");
            }

            try {
                // Extract user information from token
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);

                securityLogger.info("AUTH SUCCESS: User '{}' with roles '{}' accessing {} {} from {}", 
                    username, roles, method, path, clientIP);

                // Add user information to request headers for downstream services
                // Downstream services should TRUST these headers from the gateway
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Username", username)
                        .header("X-User-Roles", roles)
                        .header("X-Auth-Valid", "true")
                        .header("X-Gateway-Validated", "true")
                        .header("X-Client-IP", clientIP)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                securityLogger.error("AUTH ERROR: Token processing failed for {} {} from {}: {}", 
                    method, path, clientIP, e.getMessage());
                return unauthorizedResponse(response, "Token validation failed: " + e.getMessage());
            }
        };
    }

    /**
     * Checks if the endpoint is public (doesn't require authentication).
     * 
     * SECURITY POLICY: Only these endpoints are truly public
     * - Login/Register operations
     * - Health checks  
     * - Swagger documentation (dev only)
     * 
     * ALL OTHER ENDPOINTS REQUIRE VALID JWT TOKEN
     * 
     * @param path Request path
     * @return true if endpoint is public
     */
    private boolean isPublicEndpoint(String path) {
        // ===== AUTHENTICATION ENDPOINTS (PUBLIC) =====
        if (path.startsWith("/auth/login") ||
            path.startsWith("/auth/register") ||
            path.startsWith("/auth/forgot-password") ||
            path.startsWith("/auth/reset-password")) {
            return true;
        }
        
        // ===== HEALTH CHECK (PUBLIC) =====
        if (path.equals("/") ||
            path.startsWith("/actuator/health") ||
            path.startsWith("/health")) {
            return true;
        }
        
        // ===== DOCUMENTATION (DEV ONLY - Should be disabled in production) =====
        if (path.contains("/swagger") ||
            path.contains("/api-docs") ||
            path.startsWith("/v3/api-docs")) {
            return true;
        }
        
        // ===== STATIC RESOURCES (if any) =====
        if (path.startsWith("/static/") ||
            path.startsWith("/public/") ||
            path.startsWith("/assets/")) {
            return true;
        }
          // ===== ALL OTHER ENDPOINTS REQUIRE AUTHENTICATION =====
        // This includes:
        // - /auth/validate (should be internal only)
        // - /auth/refresh (requires valid refresh token)
        // - /auth/profile (requires authentication)
        // - /api/** (all API endpoints)
        return false;
    }

    /**
     * Returns unauthorized response with security logging.
     * 
     * @param response HTTP response
     * @param message Error message
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Auth-Status", "FAILED");
        
        String body = String.format(
            "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"timestamp\":\"%s\"}", 
            message, java.time.Instant.now().toString()
        );
        
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    /**
     * Extract client IP address from request headers.
     * Handles X-Forwarded-For, X-Real-IP headers for proxied requests.
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIP(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    public static class Config {
        // Configuration properties if needed
    }
}
