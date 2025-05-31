package com.example;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator-specific security configuration.
 * Only handles health endpoint security, main security is in SecurityConfig.
 */
@Configuration
public class ActuatorSecurityConfig {

  /**
   * Security configuration for actuator endpoints only.
   * Allows anonymous access to health endpoint.
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
}
