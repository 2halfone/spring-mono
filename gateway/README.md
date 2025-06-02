# Gateway Service - Spring Microservices

## 📋 Overview

The **Gateway Service** is a Spring Cloud Gateway microservice that acts as a single entry point for the Spring Microservices architecture. It provides intelligent routing, load balancing, circuit breaker pattern implementation, and centralized security management for the auth-service backend.

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.4.5
- **Java Version**: 17
- **Gateway**: Spring Cloud Gateway 2023.0.2
- **Circuit Breaker**: Resilience4J
- **Transport Protocol**: Reactive WebFlux (Non-blocking)
- **Containerization**: Docker
- **Build Tool**: Maven 3.9.6

### Main Dependencies
- `spring-cloud-starter-gateway` - Core gateway functionality
- `spring-cloud-starter-circuitbreaker-reactor-resilience4j` - Circuit breaker pattern
- `spring-boot-starter-actuator` - Health checks and monitoring
- `reactor-test` - Reactive testing support
- `spring-boot-starter-test` - Testing framework

## 📁 Project Structure

```
gateway/
├── complete/                                   # Complete implementation example
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tuo/progetto/gateway/
│   │   │   │   └── Application.java           # Complete application with routes
│   │   │   └── resources/
│   │   │       ├── application.properties     # Main configuration
│   │   │       ├── application-dev.properties # Development configuration
│   │   │       ├── application-prod.properties# Production configuration
│   │   │       ├── application-staging.properties # Staging configuration
│   │   │       └── application.yml            # YAML configuration
│   │   └── test/
│   │       ├── java/gateway/
│   │       │   └── ApplicationTest.java       # Integration tests
│   │       └── resources/
│   │           └── application-test.properties # Test configuration
│   └── pom.xml                                # Maven dependencies
├── initial/                                   # Initial implementation
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/gateway/
│   │   │   │   └── Application.java           # Basic application class
│   │   │   └── resources/
│   │   │       ├── application.properties     # Main configuration
│   │   │       ├── application-dev.properties # Development configuration
│   │   │       ├── application-prod.properties# Production configuration
│   │   │       ├── application-staging.properties # Staging configuration
│   │   │       └── application.yml            # YAML configuration
│   │   └── test/
│   │       ├── java/com/tuo/progetto/gateway/
│   │       │   └── ApplicationTests.java      # Basic tests
│   │       └── resources/
│   │           └── application-test.properties # Test configuration
│   ├── Dockerfile                             # Alternative Dockerfile
│   └── pom.xml                                # Maven dependencies
├── Dockerfile                                 # Main container configuration
├── README.adoc                                # AsciiDoc documentation
├── CONTRIBUTING.adoc                          # Contribution guidelines
├── LICENSE.txt                                # License information
└── README.md                                  # This documentation
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (optional)
- Running auth-service instance

### 1. Local Development

```bash
# Clone repository
git clone https://github.com/2halfone/spring-mono.git
cd spring-mono/gateway/initial

# Build project
./mvnw clean package -DskipTests

# Start service
./mvnw spring-boot:run
```

Service will be available at: `http://localhost:8080`

### 2. Docker Execution

```bash
# Build Docker image
cd gateway
docker build -t gateway-service .

# Run container
docker run -p 8080:8080 gateway-service
```

### 3. Docker Compose (Complete Environment)

```bash
# Development environment
docker-compose -f docker-compose.dev.yml up

# Staging environment
docker-compose -f docker-compose.staging.yml up
```

## 🔌 Gateway Routing & API Endpoints

### Route Configuration

The gateway implements intelligent routing based on path predicates:

| Route ID | Path Pattern | Target Service | Target URI | Description |
|----------|--------------|----------------|------------|-------------|
| `auth` | `/auth/**` | auth-service | `http://auth-service:8080` | Authentication and user management |

### Routing Examples

```bash
# Route to auth-service (user management)
curl http://localhost:8080/auth/users

# Health check through gateway
curl http://localhost:8080/actuator/health

# Remote access (production)
curl http://34.140.122.146:9080/auth/profile
curl http://34.140.122.146:9080/api/data
```

### Gateway Features

#### 1. Path-Based Routing
- **Predicate**: `Path=/auth/**` routes to auth-service
- **Predicate**: `Path=/api/**` routes to API services
- **Optional**: `StripPrefix=1` filter to remove route prefix

#### 2. Load Balancing
- Automatic load balancing across service instances
- Support for multiple backend instances per service

#### 3. Circuit Breaker Pattern
- Resilience4J implementation for fault tolerance
- Configurable timeout and fallback mechanisms
- Protection against cascading failures

#### 4. CORS Support
```properties
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE
```

## ⚙️ Configuration

### Configuration Profiles

#### Default Profile (application.properties)
```properties
spring.application.name=gateway-service
server.port=8080

# Management endpoints
management.endpoints.web.exposure.include=health,info

# Route to auth-service
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://auth-service:8080
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
```

#### Development Profile (application-dev.properties)
- Routes to localhost services on different ports
- Enhanced logging and debugging
- Local service discovery

#### Staging/Production Profiles
```properties
# Staging configuration
spring.cloud.gateway.routes[0].id=auth
spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**
spring.cloud.gateway.routes[0].uri=http://auth-service:8080
```

### Environment Variables

```env
# Profile selection
SPRING_PROFILES_ACTIVE=staging

# Gateway-specific configuration
SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://auth-service:8080
SPRING_CLOUD_GATEWAY_ROUTES_1_URI=http://api-service:8080

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_EXAMPLE_GATEWAY=DEBUG
```

## 🛡️ Security Analysis

### 🔒 Current Security Implementation

#### 1. **Gateway-Level Security**
- **Status**: ⚠️ **BASIC** - Minimal security implementation
- **Current Features**:
  - CORS configuration for cross-origin requests
  - Basic actuator endpoint exposure (`health`, `info`)
  - Request/response logging capabilities

#### 2. **Authentication & Authorization**
- **Status**: ❌ **MISSING** - No authentication at gateway level
- **Current State**: 
  - No JWT token validation
  - No OAuth2 integration
  - No user session management
  - Relies entirely on downstream service security

#### 3. **Transport Security**
- **Status**: ⚠️ **PARTIAL** - HTTP only
- **Current Implementation**:
  - HTTP-only communication (port 8080)
  - No TLS/SSL termination at gateway
  - No certificate management

#### 4. **Input Validation & Sanitization**
- **Status**: ❌ **MISSING** - No input validation
- **Risk Areas**:
  - No request size limits
  - No header validation
  - No path sanitization
  - No rate limiting

### 🚨 Security Vulnerabilities & Risks

#### **HIGH RISK**

1. **No Authentication Layer**
   ```
   Risk: Unauthenticated access to all backend services
   Impact: Data breach, unauthorized operations
   Mitigation: Implement JWT validation filter
   ```

2. **Missing HTTPS/TLS**
   ```
   Risk: Man-in-the-middle attacks, data interception
   Impact: Credential theft, data tampering
   Mitigation: SSL/TLS termination at gateway
   ```

3. **No Rate Limiting**
   ```
   Risk: DDoS attacks, resource exhaustion
   Impact: Service unavailability
   Mitigation: Implement Redis-based rate limiting
   ```

#### **MEDIUM RISK**

4. **CORS Wildcard Configuration**
   ```
   Current: allowedOrigins=* 
   Risk: Cross-site request forgery
   Mitigation: Specific origin whitelist
   ```

5. **No Request Size Limits**
   ```
   Risk: Memory exhaustion attacks
   Impact: Service denial
   Mitigation: Configure max request size
   ```

6. **Missing Security Headers**
   ```
   Risk: XSS, clickjacking, MIME sniffing
   Impact: Client-side attacks
   Mitigation: Add security headers filter
   ```

#### **LOW RISK**

7. **Information Disclosure**
   ```
   Current: Debug logging in production
   Risk: Sensitive data exposure in logs
   Mitigation: Production logging configuration
   ```

### 🔧 Security Recommendations

#### **Phase 1: Immediate Security (Critical)**

1. **Implement Authentication Filter**
```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // JWT validation logic
        return chain.filter(exchange);
    }
}
```

2. **Enable HTTPS/TLS**
```properties
server.ssl.enabled=true
server.ssl.key-store=gateway-keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

3. **Configure Rate Limiting**
```properties
spring.cloud.gateway.filter.request-rate-limiter.redis-rate-limiter.replenish-rate=10
spring.cloud.gateway.filter.request-rate-limiter.redis-rate-limiter.burst-capacity=20
```

#### **Phase 2: Enhanced Security (Important)**

4. **Add Security Headers**
```java
@Bean
public RouteLocator securityRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route(r -> r.path("/**")
            .filters(f -> f.modifyResponseHeaders(h -> {
                h.add("X-Frame-Options", "DENY");
                h.add("X-Content-Type-Options", "nosniff");
                h.add("X-XSS-Protection", "1; mode=block");
                h.add("Strict-Transport-Security", "max-age=31536000");
            }))
            .uri("http://backend-service"))
        .build();
}
```

5. **Implement Request Validation**
```properties
spring.cloud.gateway.filter.request-size.maxInMemorySize=1MB
spring.cloud.gateway.httpclient.max-header-size=16KB
```

6. **Secure CORS Configuration**
```properties
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=https://yourdomain.com,https://api.yourdomain.com
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=Authorization,Content-Type
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=true
```

#### **Phase 3: Advanced Security (Recommended)**

7. **API Gateway Security**
```java
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/auth/public/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerSpec::jwt)
            .build();
    }
}
```

8. **Circuit Breaker Security**
```properties
resilience4j.circuitbreaker.instances.auth-service.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.auth-service.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.auth-service.permitted-number-of-calls-in-half-open-state=3
```

### 🔍 Security Monitoring & Logging

#### **Security Event Logging**
```properties
# Security-focused logging
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.cloud.gateway.filter.ratelimit=INFO

# Audit logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [%X{traceId}] [%X{userId}] %msg%n
```

#### **Health Checks & Metrics**
```properties
# Enhanced actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,gateway

# Security metrics
management.metrics.export.prometheus.enabled=true
management.endpoint.metrics.enabled=true
```

### 🎯 Security Compliance Checklist

- [ ] **Authentication**: Implement JWT/OAuth2 validation
- [ ] **Authorization**: Role-based access control
- [ ] **Transport Security**: HTTPS/TLS encryption
- [ ] **Input Validation**: Request size and format validation
- [ ] **Rate Limiting**: DDoS protection
- [ ] **Security Headers**: XSS, CSRF protection
- [ ] **CORS Configuration**: Specific origin allowlist
- [ ] **Audit Logging**: Security event tracking
- [ ] **Circuit Breakers**: Fault tolerance
- [ ] **Secret Management**: Secure credential storage
- [ ] **Penetration Testing**: Regular security assessments
- [ ] **Vulnerability Scanning**: Dependency analysis

## 🐳 Containerization

### Multi-stage Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=initial/target/gateway-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Container Configuration

- **Base Image**: Eclipse Temurin 17 JDK Alpine
- **Port Mapping**: `8080:8080` (internal:external)
- **Health Checks**: Via actuator endpoints
- **Environment Variables**: External configuration support

### Docker Compose Integration

```yaml
gateway:
  build: ./gateway
  depends_on:
    - auth-service
    - chat-service
  ports:
    - "9080:8080"  # Staging environment
  environment:
    SPRING_PROFILES_ACTIVE: staging
```

## 🚀 Deployment

### CI/CD Pipeline

The gateway service is integrated into GitHub Actions workflow:

```yaml
# Build Matrix includes gateway-service
matrix:
  service:
    - auth-service
    - chat-service
    - gateway-service
```

### Deployment Environments

1. **Development**: `docker-compose.dev.yml`
   - Port: `8080:8080`
   - Local service routing
   - Enhanced debugging

2. **Staging**: `docker-compose.staging.yml`
   - Port: `9080:8080`
   - Container-to-container routing
   - Production-like configuration

3. **Production**: 
   - IP: `34.140.122.146:9080`
   - Container Registry: GitHub Container Registry (GHCR)
   - Load balancer integration

### Port Mapping Summary

| Environment | Host Port | Container Port | Purpose |
|-------------|-----------|----------------|---------|
| Development | 8080 | 8080 | Local development |
| Staging | 9080 | 8080 | Staging environment |
| Production | 9080 | 8080 | Production access |

## 🔍 Monitoring & Debugging

### Health Checks

```bash
# Gateway health check
curl http://localhost:8080/actuator/health

# Gateway routes information
curl http://localhost:8080/actuator/gateway/routes

# Check port binding
sudo ss -tlnp | grep 8080
```

### Gateway-Specific Monitoring

```bash
# Route discovery
curl http://localhost:8080/actuator/gateway/routes

# Circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# Gateway metrics
curl http://localhost:8080/actuator/metrics
```

### Logging & Debugging

```bash
# Docker logs
docker logs gateway-service

# Real-time logs
docker logs -f gateway-service

# Production logs (systemd)
sudo journalctl -u gateway-service -f
```

### Request Tracing

```properties
# Enable request/response logging
spring.cloud.gateway.httpclient.wiretap=true
spring.cloud.gateway.httpserver.wiretap=true

# Correlation ID tracing
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n
```

### Troubleshooting

1. **Route Not Found (404)**
   - Check route configuration in `application-{profile}.properties`
   - Verify predicate path patterns
   - Ensure target services are running

2. **Service Unavailable (503)**
   - Check backend service health
   - Verify circuit breaker configuration
   - Check network connectivity between containers

3. **Timeout Issues**
   - Review circuit breaker timeout settings
   - Check backend service performance
   - Verify network latency

4. **CORS Issues**
   - Review CORS configuration
   - Check allowed origins and methods
   - Verify preflight request handling

## 🧪 Testing

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class GatewayIntegrationTest {
    
    @Autowired
    private WebTestClient webClient;
    
    @Test
    public void testAuthServiceRouting() {
        stubFor(get(urlEqualTo("/profile"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"username\":\"testuser\",\"roles\":[\"USER\"]}")));
        
        webClient.get()
            .uri("/auth/profile")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser");
    }
}
```

### Load Testing

```bash
# Install Artillery for load testing
npm install -g artillery

# Create load test configuration
cat > gateway-load-test.yml << EOF
config:
  target: 'http://localhost:8080'
  phases:
    - duration: 60
      arrivalRate: 10
scenarios:
  - name: "Gateway routing test"
    flow:
      - get:
          url: "/auth/profile"
      - get:
          url: "/auth/me"
EOF

# Run load test
artillery run gateway-load-test.yml
```

## 🔮 Future Roadmap

### Planned Features

1. **Advanced Security**
   - JWT token validation and refresh
   - OAuth2 integration with external providers
   - Role-based access control (RBAC)
   - API key authentication

2. **Enhanced Routing**
   - Dynamic route configuration
   - Service discovery integration
   - Weighted routing and canary deployments
   - GraphQL gateway support

3. **Observability**
   - Distributed tracing with Zipkin/Jaeger
   - Prometheus metrics collection
   - Grafana dashboards
   - Application Performance Monitoring (APM)

4. **Performance Optimization**
   - HTTP/2 support
   - Request/response caching
   - Connection pooling optimization
   - GraalVM native image support

### Technical Improvements

1. **Scalability**
   - Horizontal pod autoscaling
   - Redis-based session clustering
   - Database connection pooling
   - CDN integration

2. **Resilience**
   - Advanced circuit breaker patterns
   - Bulkhead isolation
   - Retry mechanisms with exponential backoff
   - Chaos engineering testing

3. **DevOps Integration**
   - Kubernetes deployment manifests
   - Helm charts
   - Service mesh integration (Istio)
   - Blue-green deployment support

## 📞 Support

For questions or issues:

1. **Issues**: GitHub repository issues
2. **Documentation**: This README and `README.adoc`
3. **Logs**: Actuator endpoints for diagnostics
4. **Monitoring**: Gateway-specific metrics and health checks

---

**Version**: 0.0.1-SNAPSHOT  
**Last Modified**: May 2025  
**Maintainer**: Spring Microservices Team

---

# Gateway Service - Spring Microservices (Italiano)

## 📋 Panoramica

Il **Gateway Service** è un microservizio Spring Cloud Gateway che funge da punto di ingresso unico per l'architettura Spring Microservices. Fornisce routing intelligente, bilanciamento del carico, implementazione del pattern circuit breaker e gestione centralizzata della sicurezza per il servizio backend auth-service.

## 🏗️ Architettura

### Stack Tecnologico
- **Framework**: Spring Boot 3.4.5
- **Java Version**: 17
- **Gateway**: Spring Cloud Gateway 2023.0.2
- **Circuit Breaker**: Resilience4J
- **Protocollo di Trasporto**: Reactive WebFlux (Non-bloccante)
- **Containerization**: Docker
- **Build Tool**: Maven 3.9.6

### Dipendenze Principali
- `spring-cloud-starter-gateway` - Funzionalità core del gateway
- `spring-cloud-starter-circuitbreaker-reactor-resilience4j` - Pattern circuit breaker
- `spring-boot-starter-actuator` - Health checks e monitoraggio
- `reactor-test` - Supporto testing reattivo
- `spring-boot-starter-test` - Framework di testing

## 📁 Struttura del Progetto

```
gateway/
├── complete/                                   # Implementazione completa
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tuo/progetto/gateway/
│   │   │   │   └── Application.java           # Applicazione completa con routes
│   │   │   └── resources/
│   │   │       ├── application.properties     # Configurazione principale
│   │   │       ├── application-dev.properties # Configurazione sviluppo
│   │   │       ├── application-prod.properties# Configurazione produzione
│   │   │       ├── application-staging.properties # Configurazione staging
│   │   │       └── application.yml            # Configurazione YAML
│   │   └── test/
│   │       ├── java/gateway/
│   │       │   └── ApplicationTest.java       # Test di integrazione
│   │       └── resources/
│   │           └── application-test.properties # Configurazione test
│   └── pom.xml                                # Dipendenze Maven
├── initial/                                   # Implementazione iniziale
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/gateway/
│   │   │   │   └── Application.java           # Classe applicazione base
│   │   │   └── resources/
│   │   │       ├── application.properties     # Configurazione principale
│   │   │       ├── application-dev.properties # Configurazione sviluppo
│   │   │       ├── application-prod.properties# Configurazione produzione
│   │   │       ├── application-staging.properties # Configurazione staging
│   │   │       └── application.yml            # Configurazione YAML
│   │   └── test/
│   │       ├── java/com/tuo/progetto/gateway/
│   │       │   └── ApplicationTests.java      # Test base
│   │       └── resources/
│   │           └── application-test.properties # Configurazione test
│   ├── Dockerfile                             # Dockerfile alternativo
│   └── pom.xml                                # Dipendenze Maven
├── Dockerfile                                 # Configurazione container principale
├── README.adoc                                # Documentazione AsciiDoc
├── CONTRIBUTING.adoc                          # Linee guida contribuzione
├── LICENSE.txt                                # Informazioni licenza
└── README.md                                  # Questa documentazione
```

## 🚀 Avvio Rapido

### Prerequisiti
- Java 17+
- Maven 3.6+
- Docker (opzionale)
- Istanze di auth-service in esecuzione

### 1. Sviluppo Locale

```bash
# Clone del repository
git clone https://github.com/2halfone/spring-mono.git
cd spring-mono/gateway/initial

# Build del progetto
./mvnw clean package -DskipTests

# Avvio del servizio
./mvnw spring-boot:run
```

Il servizio sarà disponibile su: `http://localhost:8080`

### 2. Esecuzione con Docker

```bash
# Build dell'immagine Docker
cd gateway
docker build -t gateway-service .

# Esecuzione del container
docker run -p 8080:8080 gateway-service
```

### 3. Docker Compose (Ambiente Completo)

```bash
# Ambiente di sviluppo
docker-compose -f docker-compose.dev.yml up

# Ambiente di staging
docker-compose -f docker-compose.staging.yml up
```

## 🔌 Routing Gateway & API Endpoints

### Configurazione delle Route

Il gateway implementa routing intelligente basato su predicati di path:

| Route ID | Pattern Path | Servizio Target | URI Target | Descrizione |
|----------|--------------|-----------------|------------|-------------|
| `auth` | `/auth/**` | auth-service | `http://auth-service:8080` | Autenticazione e gestione utenti |

### Esempi di Routing

```bash
# Route verso auth-service (gestione utenti)
curl http://localhost:8080/auth/users

# Health check attraverso gateway
curl http://localhost:8080/actuator/health

# Accesso remoto (produzione)
curl http://34.140.122.146:9080/auth/users
curl http://34.140.122.146:9080/chat/messages
```

### Funzionalità del Gateway

#### 1. Routing Basato su Path
- **Predicato**: `Path=/auth/**` instrada verso auth-service
- **Predicato**: `Path=/chat/**` instrada verso chat-service
- **Opzionale**: Filtro `StripPrefix=1` per rimuovere il prefisso di route

#### 2. Bilanciamento del Carico
- Bilanciamento automatico del carico tra istanze di servizio
- Supporto per multiple istanze backend per servizio

#### 3. Pattern Circuit Breaker
- Implementazione Resilience4J per tolleranza ai guasti
- Meccanismi di timeout e fallback configurabili
- Protezione contro guasti a cascata

#### 4. Supporto CORS
```properties
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE
```

## ⚙️ Configurazione

### Profili di Configurazione

#### Default Profile (application.properties)
```properties
spring.application.name=gateway-service
server.port=8080

# Management endpoints
management.endpoints.web.exposure.include=health,info

# Route verso auth-service
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://auth-service:8080
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**

```

#### Development Profile (application-dev.properties)
- Route verso servizi localhost su porte diverse
- Logging e debugging avanzati
- Service discovery locale

#### Profili Staging/Production
```properties
# Configurazione staging
spring.cloud.gateway.routes[0].id=auth
spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**
spring.cloud.gateway.routes[0].uri=http://auth-service:8080

spring.cloud.gateway.routes[1].id=chat
spring.cloud.gateway.routes[0].uri=http://auth-service:8080
```

### Variabili di Ambiente

```env
# Selezione profilo
SPRING_PROFILES_ACTIVE=staging

# Configurazione specifica gateway
SPRING_CLOUD_GATEWAY_ROUTES_0_URI=http://auth-service:8080

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_EXAMPLE_GATEWAY=DEBUG
```

## 🛡️ Analisi di Sicurezza

### 🔒 Implementazione di Sicurezza Attuale

#### 1. **Sicurezza a Livello Gateway**
- **Status**: ⚠️ **BASILARE** - Implementazione di sicurezza minimale
- **Funzionalità Attuali**:
  - Configurazione CORS per richieste cross-origin
  - Esposizione endpoint actuator base (`health`, `info`)
  - Capacità di logging richieste/risposte

#### 2. **Autenticazione e Autorizzazione**
- **Status**: ❌ **MANCANTE** - Nessuna autenticazione a livello gateway
- **Stato Attuale**:
  - Nessuna validazione token JWT
  - Nessuna integrazione OAuth2
  - Nessuna gestione sessioni utente
  - Dipende completamente dalla sicurezza dei servizi downstream

#### 3. **Sicurezza del Trasporto**
- **Status**: ⚠️ **PARZIALE** - Solo HTTP
- **Implementazione Attuale**:
  - Comunicazione solo HTTP (porta 8080)
  - Nessuna terminazione TLS/SSL al gateway
  - Nessuna gestione certificati

#### 4. **Validazione e Sanitizzazione Input**
- **Status**: ❌ **MANCANTE** - Nessuna validazione input
- **Aree di Rischio**:
  - Nessun limite dimensione richieste
  - Nessuna validazione header
  - Nessuna sanitizzazione path
  - Nessun rate limiting

### 🚨 Vulnerabilità di Sicurezza e Rischi

#### **RISCHIO ALTO**

1. **Nessun Livello di Autenticazione**
   ```
   Rischio: Accesso non autenticato a tutti i servizi backend
   Impatto: Violazione dati, operazioni non autorizzate
   Mitigazione: Implementare filtro validazione JWT
   ```

2. **HTTPS/TLS Mancante**
   ```
   Rischio: Attacchi man-in-the-middle, intercettazione dati
   Impatto: Furto credenziali, manomissione dati
   Mitigazione: Terminazione SSL/TLS al gateway
   ```

3. **Nessun Rate Limiting**
   ```
   Rischio: Attacchi DDoS, esaurimento risorse
   Impatto: Indisponibilità servizio
   Mitigazione: Implementare rate limiting basato su Redis
   ```

#### **RISCHIO MEDIO**

4. **Configurazione CORS Wildcard**
   ```
   Attuale: allowedOrigins=*
   Rischio: Cross-site request forgery
   Mitigazione: Whitelist origini specifiche
   ```

5. **Nessun Limite Dimensione Richieste**
   ```
   Rischio: Attacchi esaurimento memoria
   Impatto: Denial of service
   Mitigazione: Configurare dimensione massima richieste
   ```

6. **Header di Sicurezza Mancanti**
   ```
   Rischio: XSS, clickjacking, MIME sniffing
   Impatto: Attacchi lato client
   Mitigazione: Aggiungere filtro header sicurezza
   ```

#### **RISCHIO BASSO**

7. **Divulgazione Informazioni**
   ```
   Attuale: Debug logging in produzione
   Rischio: Esposizione dati sensibili nei log
   Mitigazione: Configurazione logging produzione
   ```

### 🔧 Raccomandazioni di Sicurezza

#### **Fase 1: Sicurezza Immediata (Critica)**

1. **Implementare Filtro Autenticazione**
```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Logica validazione JWT
        return chain.filter(exchange);
    }
}
```

2. **Abilitare HTTPS/TLS**
```properties
server.ssl.enabled=true
server.ssl.key-store=gateway-keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

3. **Configurare Rate Limiting**
```properties
spring.cloud.gateway.filter.request-rate-limiter.redis-rate-limiter.replenish-rate=10
spring.cloud.gateway.filter.request-rate-limiter.redis-rate-limiter.burst-capacity=20
```

#### **Fase 2: Sicurezza Avanzata (Importante)**

4. **Aggiungere Header di Sicurezza**
```java
@Bean
public RouteLocator securityRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route(r -> r.path("/**")
            .filters(f -> f.modifyResponseHeaders(h -> {
                h.add("X-Frame-Options", "DENY");
                h.add("X-Content-Type-Options", "nosniff");
                h.add("X-XSS-Protection", "1; mode=block");
                h.add("Strict-Transport-Security", "max-age=31536000");
            }))
            .uri("http://backend-service"))
        .build();
}
```

5. **Implementare Validazione Richieste**
```properties
spring.cloud.gateway.filter.request-size.maxInMemorySize=1MB
spring.cloud.gateway.httpclient.max-header-size=16KB
```

6. **Configurazione CORS Sicura**
```properties
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=https://youromain.com,https://api.yourdomain.com
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=Authorization,Content-Type
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=true
```

### 🔍 Monitoraggio e Logging di Sicurezza

#### **Logging Eventi di Sicurezza**
```properties
# Logging focalizzato sulla sicurezza
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.cloud.gateway.filter.ratelimit=INFO

# Audit logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [%X{traceId}] [%X{userId}] %msg%n
```

#### **Health Checks e Metriche**
```properties
# Endpoint actuator avanzati
management.endpoints.web.exposure.include=health,info,metrics,gateway

# Metriche di sicurezza
management.metrics.export.prometheus.enabled=true
management.endpoint.metrics.enabled=true
```

### 🎯 Checklist Conformità Sicurezza

- [ ] **Autenticazione**: Implementare validazione JWT/OAuth2
- [ ] **Autorizzazione**: Controllo accesso basato su ruoli
- [ ] **Sicurezza Trasporto**: Crittografia HTTPS/TLS
- [ ] **Validazione Input**: Validazione dimensione e formato richieste
- [ ] **Rate Limiting**: Protezione DDoS
- [ ] **Header Sicurezza**: Protezione XSS, CSRF
- [ ] **Configurazione CORS**: Allowlist origini specifiche
- [ ] **Audit Logging**: Tracking eventi sicurezza
- [ ] **Circuit Breakers**: Tolleranza ai guasti
- [ ] **Gestione Segreti**: Storage sicuro credenziali
- [ ] **Penetration Testing**: Valutazioni sicurezza regolari
- [ ] **Vulnerability Scanning**: Analisi dipendenze

## 🐳 Containerization

### Multi-stage Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=initial/target/gateway-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Configurazione Container

- **Immagine Base**: Eclipse Temurin 17 JDK Alpine
- **Port Mapping**: `8080:8080` (interno:esterno)
- **Health Checks**: Via actuator endpoints
- **Variabili di Ambiente**: Supporto configurazione esterna

### Integrazione Docker Compose

```yaml
gateway:
  build: ./gateway
  depends_on:
    - auth-service
    - chat-service
  ports:
    - "9080:8080"  # Ambiente staging
  environment:
    SPRING_PROFILES_ACTIVE: staging
```

## 🚀 Deployment

### Pipeline CI/CD

Il servizio gateway è integrato nel workflow GitHub Actions:

```yaml
# Build Matrix include gateway-service
matrix:
  service:
    - auth-service
    - gateway-service
```

### Ambienti di Deployment

1. **Development**: `docker-compose.dev.yml`
   - Port: `8080:8080`
   - Routing servizi locali
   - Debugging avanzato

2. **Staging**: `docker-compose.staging.yml`
   - Port: `9080:8080`
   - Routing container-to-container
   - Configurazione production-like

3. **Production**: 
   - IP: `34.140.122.146:9080`
   - Container Registry: GitHub Container Registry (GHCR)
   - Integrazione load balancer

### Riepilogo Port Mapping

| Ambiente | Host Port | Container Port | Scopo |
|----------|-----------|----------------|-------|
| Development | 8080 | 8080 | Sviluppo locale |
| Staging | 9080 | 8080 | Ambiente staging |
| Production | 9080 | 8080 | Accesso produzione |

## 🔍 Monitoraggio & Debugging

### Health Checks

```bash
# Health check gateway
curl http://localhost:8080/actuator/health

# Informazioni route gateway
curl http://localhost:8080/actuator/gateway/routes

# Verifica binding porta
sudo ss -tlnp | grep 8080
```

### Monitoraggio Specifico Gateway

```bash
# Scoperta route
curl http://localhost:8080/actuator/gateway/routes

# Stato circuit breaker
curl http://localhost:8080/actuator/circuitbreakers

# Metriche gateway
curl http://localhost:8080/actuator/metrics
```

### Logging & Debugging

```bash
# Log Docker
docker logs gateway-service

# Log real-time
docker logs -f gateway-service

# Log produzione (systemd)
sudo journalctl -u gateway-service -f
```

### Troubleshooting

1. **Route Non Trovata (404)**
   - Verificare configurazione route in `application-{profile}.properties`
   - Controllare pattern path predicati
   - Assicurarsi che i servizi target siano in esecuzione

2. **Servizio Non Disponibile (503)**
   - Verificare health dei servizi backend
   - Controllare configurazione circuit breaker
   - Verificare connettività di rete tra container

3. **Problemi di Timeout**
   - Rivedere impostazioni timeout circuit breaker
   - Verificare performance servizi backend
   - Controllare latenza di rete

4. **Problemi CORS**
   - Rivedere configurazione CORS
   - Verificare origini e metodi consentiti
   - Controllare gestione richieste preflight

## 🔮 Roadmap Futuro

### Funzionalità Pianificate

1. **Sicurezza Avanzata**
   - Validazione e refresh token JWT
   - Integrazione OAuth2 con provider esterni
   - Controllo accesso basato su ruoli (RBAC)
   - Autenticazione API key

2. **Routing Avanzato**
   - Configurazione route dinamica
   - Integrazione service discovery
   - Routing pesato e canary deployment
   - Supporto gateway GraphQL

3. **Osservabilità**
   - Tracing distribuito con Zipkin/Jaeger
   - Raccolta metriche Prometheus
   - Dashboard Grafana
   - Application Performance Monitoring (APM)

4. **Ottimizzazione Performance**
   - Supporto HTTP/2
   - Caching richieste/risposte
   - Ottimizzazione connection pooling
   - Supporto immagini native GraalVM

## 📞 Supporto

Per domande o problemi:

1. **Issues**: GitHub repository issues
2. **Documentazione**: Questo README e `README.adoc`
3. **Logs**: Actuator endpoints per diagnostica
4. **Monitoraggio**: Metriche specifiche gateway e health checks

---

**Versione**: 0.0.1-SNAPSHOT  
**Ultima Modifica**: Maggio 2025  
**Maintainer**: Spring Microservices Team
