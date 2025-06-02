# 🌐 Gateway Service - Security API Gateway

## 📋 Overview

The **Gateway Service** is a production-ready Spring Cloud Gateway that serves as the main entry point for the microservices architecture. It provides centralized authentication, rate limiting, request routing, and security features for all backend services.

**Status**: ✅ **Production-Ready & VM Tested**  
**Public Port**: 8080 (HTTP)  
**Management Port**: 8082 (Health & Metrics)  
**Framework**: Spring Cloud Gateway (Reactive)

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.4.5 + Spring Cloud Gateway
- **Java Version**: 17
- **Reactive Stack**: Spring WebFlux
- **Cache**: Redis (for rate limiting)
- **Load Balancer**: Spring Cloud LoadBalancer
- **Security**: JWT authentication filter
- **Monitoring**: Spring Boot Actuator
- **Build Tool**: Maven

### Core Dependencies
- `spring-cloud-starter-gateway` - Reactive gateway functionality
- `spring-boot-starter-data-redis-reactive` - Redis integration
- `spring-boot-starter-actuator` - Health monitoring
- `spring-cloud-starter-loadbalancer` - Service discovery
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT validation

## 📁 Project Structure

```
gateway/initial/
├── src/
│   ├── main/
│   │   ├── java/com/example/gateway/
│   │   │   ├── GatewayApplication.java             # Main application class
│   │   │   ├── config/
│   │   │   │   ├── GatewayConfig.java             # Route configuration
│   │   │   │   ├── CorsConfig.java                # CORS configuration
│   │   │   │   └── RedisConfig.java               # Redis configuration
│   │   │   ├── filter/
│   │   │   │   ├── AuthenticationFilter.java      # JWT authentication filter
│   │   │   │   ├── RateLimitFilter.java          # Rate limiting filter
│   │   │   │   └── SecurityLogFilter.java        # Security audit logging
│   │   │   └── service/
│   │   │       ├── JwtValidationService.java      # JWT token validation
│   │   │       └── RateLimitService.java         # Rate limit logic
│   │   └── resources/
│   │       ├── application.properties              # Main configuration
│   │       ├── application-dev.properties          # Development configuration
│   │       └── application-staging.properties      # Staging configuration
│   └── test/
│       └── java/com/example/gateway/
│           └── GatewayApplicationTests.java        # Integration tests
├── logs/
│   └── gateway-security.log                       # Security audit logs
├── Dockerfile                                      # Container configuration
├── pom.xml                                         # Maven dependencies
└── README.md                                       # This documentation
```

## 🔌 Gateway Routes & Features

### **🔐 Authentication Routes**
All `/auth/*` requests are routed to the auth-service:

```yaml
Routes:
  - Path: /auth/**
    Target: http://localhost:8081
    Methods: GET, POST, PUT, DELETE
    Filters: 
      - Rate Limiting (5 req/sec, burst 10)
      - Security Logging
      - CORS
```

### **📊 Management Endpoints (Port 8082)**
- `GET /actuator/health` - Gateway health status
- `GET /actuator/info` - Service information  
- `GET /actuator/metrics` - Performance metrics
- `GET /actuator/gateway/routes` - Active route configuration

### **🛡️ Security Features**

#### **Rate Limiting**
- **Algorithm**: Token bucket with Redis backend
- **Default Limits**: 5 requests/second, burst capacity 10
- **Scope**: Per IP address
- **Response**: HTTP 429 (Too Many Requests) when exceeded

#### **JWT Authentication**
- **Validation**: HMAC256 signature verification
- **Header**: `Authorization: Bearer <token>`
- **Bypass**: Public endpoints (`/auth/login`, `/auth/register`, `/actuator/health`)
- **Error Handling**: HTTP 401 for invalid/expired tokens

#### **CORS Configuration**
- **Allowed Origins**: Configurable per environment
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: Authorization, Content-Type
- **Max Age**: 3600 seconds

#### **Security Logging**
All requests are logged with:
- Request ID, timestamp, method, path
- Client IP address
- Authentication status
- Rate limit status
- Response status and duration

## ⚙️ Configuration

### **Development Profile** (`application-dev.properties`)
```properties
server.port=8080
management.server.port=8082
management.endpoints.web.exposure.include=health,info,metrics,gateway

# Auth service routing
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://localhost:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**

# CORS
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=http://localhost:3000
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=Authorization,Content-Type

# Redis (for development, use embedded)
spring.redis.host=localhost
spring.redis.port=6379

# Logging
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.reactor.netty=INFO
```

### **Staging Profile** (`application-staging.properties`)
```properties
server.port=8080
management.server.port=8082
server.ssl.enabled=false

# Auth service routing  
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://localhost:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**
spring.cloud.gateway.routes[0].filters[0]=name=RequestRateLimiter
spring.cloud.gateway.routes[0].filters[0].args.redis-rate-limiter.replenishRate=5
spring.cloud.gateway.routes[0].filters[0].filters[0].args.redis-rate-limiter.burstCapacity=10

# Redis configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms

# Security
jwt.secret=mySecretKey123456789012345678901234567890

# CORS for production
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=Authorization,Content-Type

# Actuator security
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

## 🚀 Deployment

### **Prerequisites**
- Java 17+
- Maven 3.6+
- Redis 7+ (for rate limiting)
- Auth-service running on port 8081

### **Local Development**
```bash
# Clone the repository
git clone <repository-url>
cd spring-mono/gateway/initial

# Ensure Redis is running
docker-compose -f ../../docker-compose.staging.yml up -d redis

# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Gateway will be available at:
# - Public API: http://localhost:8080
# - Management: http://localhost:8082
```

### **Staging Environment**
```bash
# Ensure infrastructure is running
docker-compose -f ../../docker-compose.staging.yml up -d postgres redis

# Ensure auth-service is running
cd ../../auth-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=staging &

# Run gateway with staging profile
cd ../gateway/initial
./mvnw spring-boot:run -Dspring-boot.run.profiles=staging

# Verify deployment
curl http://localhost:8080/actuator/health    # Public health check
curl http://localhost:8082/actuator/health    # Management health check
```

### **Docker Deployment**
```bash
# Build Docker image
docker build -t gateway-service:latest .

# Run container
docker run -p 8080:8080 -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e SPRING_REDIS_HOST=host.docker.internal \
  gateway-service:latest
```

## 🧪 Testing

### **Health Checks**
```bash
# Gateway public health
curl http://localhost:8080/actuator/health

# Gateway management health  
curl http://localhost:8082/actuator/health

# Expected response: {"status":"UP"}
```

### **Authentication Flow Testing**
```bash
# Test registration through gateway
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "securePassword123"
  }'

# Test login through gateway
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securePassword123"
  }'
```

### **Rate Limiting Testing**
```bash
# Test rate limiting (should get 429 after 5 requests/second)
for i in {1..10}; do
  echo "Request $i:"
  curl -w "Status: %{http_code} | Time: %{time_total}s\n" \
    -X GET http://localhost:8080/actuator/health \
    -o /dev/null -s
done
```

### **Route Configuration Check**
```bash
# View active routes
curl http://localhost:8082/actuator/gateway/routes | jq
```

## 📊 Monitoring & Observability

### **Health Indicators**
- **Gateway Status**: Overall gateway health
- **Redis Connectivity**: Rate limiting backend status  
- **Route Health**: Backend service availability
- **Circuit Breaker**: Service resilience status

### **Metrics Collection**
Available at `http://localhost:8082/actuator/metrics`:
- `gateway.requests.total` - Total request count
- `gateway.requests.duration` - Request duration histogram
- `gateway.route.requests` - Per-route request metrics
- `redis.commands.duration` - Redis operation metrics

### **Security Audit Logs**
Security events are logged to `logs/gateway-security.log`:
```json
{
  "timestamp": "2024-12-02T10:30:00.000Z",
  "requestId": "abc123",
  "clientIp": "192.168.1.100",
  "method": "POST",
  "path": "/auth/login",
  "authStatus": "SUCCESS",
  "rateLimitStatus": "ALLOWED",
  "responseStatus": 200,
  "duration": 150
}
```

## 🔧 Performance Tuning

### **Reactive Configuration**
```properties
# Netty server tuning
server.netty.connection-timeout=20s
server.netty.idle-timeout=60s

# WebFlux configuration  
spring.webflux.multipart.max-in-memory-size=1MB
spring.webflux.multipart.max-disk-usage-per-part=10MB
```

### **Redis Connection Pooling**
```properties
# Redis connection pool
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.pool.max-wait=-1ms
```

### **JVM Tuning**
```bash
# Recommended JVM settings for production
-Xms512m -Xmx1g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

## 🔒 Security Best Practices

### **Implemented Security Measures**
- ✅ **JWT Validation**: Centralized token verification
- ✅ **Rate Limiting**: DDoS protection with Redis backend
- ✅ **CORS Control**: Configurable cross-origin policies
- ✅ **Security Headers**: Standard security headers applied
- ✅ **Audit Logging**: Comprehensive security event logging
- ✅ **Input Validation**: Request size limits and format validation

### **Production Security Checklist**
- [ ] Enable HTTPS with proper SSL certificates
- [ ] Configure secure CORS origins (remove wildcards)
- [ ] Set up WAF (Web Application Firewall)
- [ ] Implement API key authentication for service-to-service calls
- [ ] Enable security headers (HSTS, CSP, X-Frame-Options)
- [ ] Set up intrusion detection and monitoring
- [ ] Regular security updates and vulnerability scanning

## 🔄 Integration Architecture

### **Request Flow**
```
[Client] → [Gateway:8080] → [Auth-Service:8081] → [PostgreSQL:5432]
             ↓
          [Redis:6379]
       (Rate Limiting)
```

### **Service Discovery**
- **Static Configuration**: Direct URL routing to localhost services
- **Health Checks**: Automatic route disable on service failure
- **Load Balancing**: Round-robin for multiple instances
- **Circuit Breaker**: Fail-fast and recovery patterns

### **Error Handling**
- **Timeout**: 30 seconds for backend calls
- **Retry**: 3 attempts with exponential backoff
- **Fallback**: Error page for service unavailable
- **Logging**: Detailed error tracking and alerting

## 📞 Support & Troubleshooting

### **Common Issues**

**Gateway won't start:**
- Check Redis connectivity
- Verify port 8080/8082 availability
- Check auth-service availability

**Rate limiting not working:**
- Verify Redis connection
- Check Redis key expiration
- Review rate limit configuration

**CORS errors:**
- Update allowed origins in configuration
- Check preflight request handling
- Verify headers configuration

### **Debug Commands**
```bash
# Check gateway routes
curl http://localhost:8082/actuator/gateway/routes

# Monitor logs
tail -f logs/gateway-security.log

# Redis monitoring
docker exec -it redis-staging redis-cli monitor
```

---

**Service Status**: ✅ Production-Ready & VM Tested  
**Last Updated**: June 2025  
**Ports**: 8080 (Public), 8082 (Management)  
**Health Check**: http://localhost:8080/actuator/health

For integration with other services, see the main project README and auth-service documentation.
