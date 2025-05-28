# Project README

## Gateway Service

### Dockerfile Location
The Dockerfile for the gateway service is located at: `gateway/Dockerfile`

### Configuration
The gateway service can be configured using environment variables and Spring profiles.

**Environment Variables:**
*   `SPRING_PROFILES_ACTIVE`: Sets the active Spring profile (e.g., `staging`, `prod`). This determines which `application-{profile}.properties` file is loaded.

**Spring Profiles:**
*   The service uses profile-specific property files like `application-staging.properties` or `application-prod.properties` located in `gateway/initial/src/main/resources/`.
*   These files contain configurations such as server port and Spring Cloud Gateway routes. For example:
    ```properties
    # Port of the gateway
    server.port=8080

    # Route to auth-service
    spring.cloud.gateway.routes[0].id=auth
    spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**
    spring.cloud.gateway.routes[0].uri=http://auth-service:8080

    # Route to chat-service
    spring.cloud.gateway.routes[1].id=chat
    spring.cloud.gateway.routes[1].predicates[0]=Path=/chat/**
    spring.cloud.gateway.routes[1].uri=http://chat-service:8080
    ```

### Exposed Ports
*   Internally, the gateway service runs on port `8080` (as configured by `server.port` in its application properties).
*   When running via Docker (e.g., using `docker-compose.staging.yml`), this internal port is typically mapped to a different host port. For example, in `docker-compose.staging.yml`, it might be mapped like `8083:8080`, meaning the gateway is accessible on port `8083` of the host machine.
