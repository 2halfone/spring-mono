package com.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Configuration for HTTPS/TLS security (Phase 1.2).
 * Handles HTTP to HTTPS redirection for security compliance.
 * 
 * Security Features:
 * - Forces HTTPS for all external traffic
 * - Redirects HTTP requests to HTTPS endpoints
 * - Maintains internal service communication over HTTP
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
                String httpsUrl = exchange.getRequest().getURI().toString().replaceFirst("http://", "https://");
                // Change port from 8080 to 8443 for HTTPS
                httpsUrl = httpsUrl.replaceFirst(":8080", ":8443");
                
                return exchange.getResponse().writeWith(
                    Mono.fromRunnable(() -> {
                        exchange.getResponse().setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                        exchange.getResponse().getHeaders().setLocation(URI.create(httpsUrl));
                    })
                ).then();
            }
            
            return chain.filter(exchange);
        };
    }

    /**
     * Additional HTTP server for health checks and redirects.
     * Keeps a simple HTTP endpoint available for load balancer health checks.
     */
    @Bean
    @ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
    public ReactiveWebServerFactory httpServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.setPort(8080);
        return factory;
    }
}
