package com.example;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for auth-service.
 * Configures JWT-based authentication endpoints.
 */
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

  /**
   * Security configuration for actuator endpoints.
   */
  @Bean
  SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    http
      // Apply this security matcher only to actuator URLs
      .securityMatcher(EndpointRequest.to(HealthEndpoint.class))
        .authorizeHttpRequests(auth -> auth
          .anyRequest().permitAll()       // Allow anonymous access to health endpoint
        )
      .csrf(csrf -> csrf.disable());     // Disable CSRF for actuator
    return http.build();
  }

  /**
   * Main security configuration for auth endpoints.
   */
  @Bean
  public SecurityFilterChain authSecurity(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/login", "/auth/validate").permitAll()
            .requestMatchers("/auth/me", "/auth/refresh").authenticated()
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
}
