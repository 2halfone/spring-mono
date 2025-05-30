package com.example.gateway;

import com.example.security.JwtUtil;
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
 * Validates JWT tokens for incoming requests (except public endpoints).
 * 
 * Security Features:
 * - Stateless JWT validation
 * - No database calls during validation
 * - Proper HTTP status codes for auth failures
 * - Claims forwarding to downstream services
 */
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

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

            // Skip authentication for public endpoints
            if (isPublicEndpoint(request.getPath().value())) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = jwtUtil.extractTokenFromHeader(authHeader);

            if (token == null) {
                return unauthorizedResponse(response, "Missing or invalid Authorization header");
            }

            // Validate JWT token
            if (!jwtUtil.validateToken(token)) {
                return unauthorizedResponse(response, "Invalid or expired JWT token");
            }

            try {
                // Extract user information from token
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Username", username)
                        .header("X-User-Roles", roles)
                        .header("X-Auth-Valid", "true")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return unauthorizedResponse(response, "Token validation failed: " + e.getMessage());
            }
        };
    }

    /**
     * Checks if the endpoint is public (doesn't require authentication).
     * 
     * @param path Request path
     * @return true if endpoint is public
     */
    private boolean isPublicEndpoint(String path) {
        // Public endpoints that don't require authentication
        return path.equals("/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/auth/login") ||
               path.startsWith("/auth/validate") ||
               path.startsWith("/auth/refresh") ||
               path.startsWith("/public/") ||
               path.contains("/swagger") ||
               path.contains("/api-docs");
    }

    /**
     * Returns unauthorized response.
     * 
     * @param response HTTP response
     * @param message Error message
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties if needed
    }
}
