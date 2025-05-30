package com.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.net.URI;

/**
 * Configuration for HTTPS/TLS security (Phase 1.2).
 * Handles HTTP to HTTPS redirection for security compliance.
 * 
 * Security Features:
 * - Forces HTTPS for all external traffic
 * - Redirects HTTP requests to HTTPS endpoints
 * - Single server configuration to avoid conflicts
 * - Enables SSL termination at gateway level
 */
@Configuration
public class HttpsRedirectConfig {

    /**
     * HTTP to HTTPS redirect filter.
     * Only active when SSL is enabled.
     */
    @Bean
    @ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
    public WebFilter httpsRedirectFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String scheme = exchange.getRequest().getURI().getScheme();
            // If request is HTTP and SSL is enabled, redirect to HTTPS
            if ("http".equals(scheme)) {
                String baseHttpsUrl = exchange.getRequest().getURI().toString().replaceFirst("http://", "https://");
                // Change port from 8080 to 8443 for HTTPS
                final String httpsUrl = baseHttpsUrl.replaceFirst(":8080", ":8443");
                
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                response.getHeaders().setLocation(URI.create(httpsUrl));
                return response.setComplete();
            }
            
            return chain.filter(exchange);
        };
    }
}
