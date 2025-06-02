# 🚀 Spring Microservices Security Gateway

## 📋 Project Overview

**Status**: ✅ **PRODUCTION-READY & TESTED**  
**Last Updated**: December 2024  
**Deployment Status**: ✅ **VM TESTED & OPERATIONAL**

This is a complete Spring Boot microservices architecture featuring a security gateway with JWT authentication, API routing, and rate limiting. The system has been successfully deployed and tested on VM infrastructure.

### **🏗️ Architecture Components:**
- **🔐 Auth Service** (Port 8081) - JWT Authentication & User Management
- **🌐 Gateway Service** (Port 8080) - Security Gateway with Rate Limiting  
- **💾 PostgreSQL Database** (Port 5432) - User Data Persistence
- **🚀 Redis Cache** (Port 6379) - Rate Limiting & Session Management

---

## 🎯 **PRODUCTION-READY FEATURES**

### **✅ IMPLEMENTED & TESTED SECURITY FEATURES**

#### **🔐 Authentication System**
- **JWT Tokens**: HMAC256 signed with configurable expiration
- **Password Security**: BCrypt hashing (work factor 12)
- **User Management**: Registration, login, profile access
- **Input Validation**: Email format, password strength validation

#### **🌐 API Gateway**
- **Reactive Gateway**: Spring Cloud Gateway with WebFlux
- **Rate Limiting**: Redis-based (5 req/sec, burst 10)
- **Request Routing**: Dynamic service discovery
- **Health Monitoring**: Actuator endpoints on port 8082

#### **🛡️ Security Infrastructure**
- **CORS Configuration**: Configurable origin policies
- **Environment Profiles**: Dev/staging/production ready
- **Database Security**: Connection pooling, encrypted credentials
- **Service Isolation**: Separate ports and network segments

---

## 🚀 **DEPLOYMENT GUIDE**

### **📋 Prerequisites**
- Java 17+ 
- Docker & Docker Compose
- Maven 3.6+
- Git

### **🔧 Quick Start**

#### **1. Clone & Setup**
```bash
git clone <repository-url>
cd spring-mono
```

#### **2. Start Infrastructure**
```bash
# Start databases using docker-compose
docker-compose -f docker-compose.staging.yml up -d postgres redis

# Verify services are running
docker ps
```

#### **3. Configure Environment**
```bash
# The staging profiles are pre-configured for localhost development
# No additional environment setup required for development
```

#### **4. Start Services**
```bash
# Terminal 1: Start auth-service (staging profile)
cd auth-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=staging

# Terminal 2: Start gateway (staging profile)  
cd gateway/initial
./mvnw spring-boot:run -Dspring-boot.run.profiles=staging
```

#### **5. Verify Deployment**
```bash
# Health checks
curl http://localhost:8081/actuator/health  # Auth-service
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8082/actuator/health  # Gateway management

# Should all return: {"status":"UP"}
```
### **🧪 Testing Guide**

#### **1. Authentication Flow Test**
```bash
# Register new user
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "securePassword123"
  }'

# Login and get JWT token
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser", 
    "password": "securePassword123"
  }'

# The response will include a JWT token for authenticated requests
```

#### **2. Gateway Routing Test**
```bash
# Test auth endpoints through gateway
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securePassword123"
  }'
```

#### **3. Rate Limiting Test**
```bash
# Test rate limiting by making multiple requests quickly
# Should start getting 429 responses after 5 requests/second
for i in {1..10}; do
  echo "Request $i:"
  curl -w "Status: %{http_code}\n" \
    -X GET http://localhost:8080/actuator/health \
    -o /dev/null -s
done
```

---

## 📊 **ARCHITECTURE OVERVIEW**

### **🔄 Request Flow**
```
[Client] → [Gateway:8080] → [Auth-Service:8081] → [PostgreSQL:5432]
             ↓
          [Redis:6379] 
       (Rate Limiting)
```

### **📊 Port Configuration**
- **Auth-Service**: 8081 (HTTP API)
- **Gateway**: 8080 (HTTP Public), 8082 (Management)
- **PostgreSQL**: 5432 (Database)
- **Redis**: 6379 (Cache & Rate Limiting)

### **🛡️ Security Layers**
1. **Gateway Layer**: Rate limiting, request routing, health checks
2. **Authentication Layer**: JWT validation, user management
3. **Data Layer**: PostgreSQL persistence, connection security
4. **Caching Layer**: Redis for rate limiting and session data

---

## 🔧 **CONFIGURATION PROFILES**

### **Development Profile** (`application-dev.properties`)
```properties
server.port=8081
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
logging.level.root=INFO
```

### **Staging Profile** (`application-staging.properties`)  
```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=auth_user
spring.datasource.password=secure_password
spring.jpa.hibernate.ddl-auto=update
```

---

## 🔧 **API ENDPOINTS**

### **🔐 Auth Service Endpoints (Port 8081)**
- `POST /auth/register` - User registration
- `POST /auth/login` - User authentication
- `GET /actuator/health` - Service health check

### **🌐 Gateway Endpoints (Port 8080)**
- `/auth/*` - Proxied to auth-service
- `GET /actuator/health` - Gateway health check

### **📊 Management Endpoints (Port 8082)**
- `GET /actuator/health` - Gateway management health
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Performance metrics

---

## 🐳 **Docker Configuration**

Both services include Dockerfiles for containerized deployment:

```dockerfile
# Example Dockerfile structure
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Use the provided `docker-compose.staging.yml` for infrastructure setup.

---

## 📝 **Project Structure**

```
spring-mono/
├── auth-service/               # Authentication microservice
│   ├── src/main/java/         # Java source code
│   ├── src/main/resources/    # Configuration files
│   ├── Dockerfile             # Container configuration
│   └── pom.xml               # Maven dependencies
├── gateway/initial/           # API Gateway service
│   ├── src/main/java/        # Java source code
│   ├── src/main/resources/   # Configuration files
│   ├── Dockerfile            # Container configuration
│   └── pom.xml              # Maven dependencies
├── docker-compose.staging.yml # Infrastructure setup
├── PROJECT_COMPLETION_SUMMARY.md
└── README.md                 # This documentation
```

---

## 🚀 **Getting Started**

1. **Clone the repository**
2. **Start infrastructure**: `docker-compose -f docker-compose.staging.yml up -d`
3. **Start auth-service**: `cd auth-service && ./mvnw spring-boot:run -Dspring-boot.run.profiles=staging`
4. **Start gateway**: `cd gateway/initial && ./mvnw spring-boot:run -Dspring-boot.run.profiles=staging`
5. **Test the setup**: `curl http://localhost:8080/actuator/health`

The system is production-ready and has been tested on VM infrastructure. All services communicate properly and handle authentication, routing, and rate limiting as expected.

---

## 📞 **Support & Documentation**

- **Project Status**: See `PROJECT_COMPLETION_SUMMARY.md`
- **VM Testing**: See `readme/test/` directory
- **Service Details**: Check individual service README files
- **Configuration**: Review `application-*.properties` files

**Last Tested**: December 2024 on VM Infrastructure  
**Status**: ✅ Production Ready & Operational
