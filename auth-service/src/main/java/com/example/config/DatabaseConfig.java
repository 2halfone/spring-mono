package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Database Configuration that automatically detects the database type
 * and sets the appropriate driver based on the datasource URL.
 * 
 * Supports:
 * - PostgreSQL (production/staging)
 * - H2 (development/testing)
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        logger.info("Configuring datasource with URL: {}", datasourceUrl);
        
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(datasourceUrl);
        dataSourceBuilder.username(datasourceUsername);
        dataSourceBuilder.password(datasourcePassword);

        // Auto-detect driver based on URL
        if (datasourceUrl.contains("postgresql")) {
            dataSourceBuilder.driverClassName("org.postgresql.Driver");
            logger.info("Using PostgreSQL driver for production/staging environment");
        } else if (datasourceUrl.contains("h2")) {
            dataSourceBuilder.driverClassName("org.h2.Driver");
            logger.info("Using H2 driver for development/testing environment");
        } else {
            logger.warn("Unknown database type in URL: {}. Using default driver detection.", datasourceUrl);
        }

        return dataSourceBuilder.build();
    }
}
