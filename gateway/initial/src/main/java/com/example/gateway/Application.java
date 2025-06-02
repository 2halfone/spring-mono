package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.gateway", "com.example.security", "com.example.config"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}