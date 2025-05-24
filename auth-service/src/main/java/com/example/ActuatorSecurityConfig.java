package com.example;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

  @Bean
  SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    http
      // applica questo security matcher solo sulle URL actuator
      .securityMatcher(EndpointRequest.to(HealthEndpoint.class))
        .authorizeHttpRequests(auth -> auth
          .anyRequest().permitAll()       // permetti anonimo
        )
      .csrf(csrf -> csrf.disable());                // disabilita CSRF per actuator
    return http.build();
  }
}
