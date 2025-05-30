package com.example.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration (Phase 1.4 Security).
 * Implements basic DDoS protection through request rate limiting.
 * 
 * Security Features:
 * - IP-based rate limiting for anonymous requests
 * - User-based rate limiting for authenticated requests
 * - Configurable limits per endpoint type
 * - Redis-backed distributed rate limiting
 * - Graduated response (429 Too Many Requests)
 */
@Configuration
public class RateLimitConfig {

    /**
     * Rate limiter for authentication endpoints.
     * More restrictive to prevent brute force attacks.
     */
    @Bean
    @Primary
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(
            5,    // replenishRate: tokens per second
            10,   // burstCapacity: maximum tokens in bucket
            1     // requestedTokens: tokens per request
        );
    }

    /**
     * Rate limiter for general API endpoints.
     * Less restrictive for normal application usage.
     */
    @Bean
    public RedisRateLimiter apiRateLimiter() {
        return new RedisRateLimiter(
            20,   // replenishRate: tokens per second
            50,   // burstCapacity: maximum tokens in bucket
            1     // requestedTokens: tokens per request
        );
    }

    /**
     * Key resolver for rate limiting.
     * Uses IP address for anonymous requests, user ID for authenticated requests.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // Check if user is authenticated (has X-User-Username header)
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
            if (username != null && !username.isEmpty()) {
                // Use username for authenticated users
                return Mono.just("user:" + username);
            }
            
            // Use IP address for anonymous requests
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Alternative key resolver based only on IP address.
     * Useful for protecting against distributed attacks.
     */
    @Bean
    public KeyResolver strictIpKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            
            return Mono.just("strict-ip:" + clientIp);
        };
    }
}
