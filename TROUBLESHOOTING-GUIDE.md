# 🔧 TROUBLESHOOTING GUIDE - JWT Security Implementation

## 📋 Overview

Questa guida documenta i problemi critici trovati durante l'implementazione della sicurezza JWT (Fase 1) e le soluzioni step-by-step per risolverli. Include comandi di troubleshooting, diagnosi e fix completi.

---

## 🚨 PROBLEMI IDENTIFICATI E SOLUZIONI

### **1. 🔴 IMPLEMENTAZIONI JWT MANCANTI NEI CONTAINER**

#### **Problema:**
```bash
# Test falliti sulla VM
curl -X POST http://localhost:9081/auth/login
# Response: 404 Not Found
```

#### **Causa:**
- File JWT esistenti localmente ma **non inclusi nei container Docker**
- Container VM built con **codice outdated** (cache Docker)
- **Missing dependencies** nel gateway

#### **Diagnosi Commands:**
```bash
# 1. Verifica esistenza file locali
ls -la auth-service/src/main/java/com/example/controller/AuthController.java
ls -la auth-service/src/main/java/com/example/service/AuthService.java
ls -la auth-service/src/main/java/com/example/security/JwtUtil.java

# 2. Verifica container content (se running)
docker exec spring-mono_auth-service_1 ls -la /app/com/example/controller/
docker exec spring-mono_auth-service_1 find /app -name "AuthController.class"

# 3. Check git status per file changes
git status --porcelain | grep -E "(AuthController|AuthService|JwtUtil)"
```

#### **Soluzione:**
```bash
# 1. Commit e push delle implementazioni JWT
git add .
git commit -m "feat: add JWT authentication implementation"
git push origin staging

# 2. Pull sulla VM e rebuild completo
git pull origin staging
docker-compose -f docker-compose.staging.yml down
docker system prune -f
docker-compose -f docker-compose.staging.yml build --no-cache
docker-compose -f docker-compose.staging.yml up -d
```

---

### **2. 🔴 DOCKER COMPOSE YAML SYNTAX ERRORS**

#### **Problema:**
```bash
docker-compose -f docker-compose.staging.yml up
# ERROR: yaml.scanner.ScannerError: mapping values are not allowed here
# in "./docker-compose.staging.yml", line 9, column 55
```

#### **Causa:**
Variabili di ambiente multiple sulla stessa riga senza newline:
```yaml
# ERRATO:
POSTGRES_USER: springuser      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-change_me}

# CORRETTO:
POSTGRES_USER: springuser
POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-change_me}
```

#### **Diagnosi Commands:**
```bash
# 1. Identifica righe problematiche
sed -n '9p' docker-compose.staging.yml
sed -n '48p' docker-compose.staging.yml

# 2. Verifica sintassi YAML
docker-compose -f docker-compose.staging.yml config

# 3. Find all lines with multiple env vars
grep -n ".*:.*      .*:" docker-compose.staging.yml
```

#### **Soluzione:**
```bash
# 1. Backup del file
cp docker-compose.staging.yml docker-compose.staging.yml.backup

# 2. Fix riga 9 (postgres service)
sed -i 's/POSTGRES_USER: springuser      POSTGRES_PASSWORD:/POSTGRES_USER: springuser\n      POSTGRES_PASSWORD:/' docker-compose.staging.yml

# 3. Fix riga 48 (chat-service)
sed -i 's/SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-change_me}      JWT_SECRET:/SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-change_me}\n      JWT_SECRET:/' docker-compose.staging.yml

# 4. Verifica correzioni
sed -n '8,12p' docker-compose.staging.yml
sed -n '46,50p' docker-compose.staging.yml

# 5. Test validità YAML
docker-compose -f docker-compose.staging.yml config
```

---

### **3. 🔴 MISSING ACTUATOR DEPENDENCY NEL GATEWAY**

#### **Problema:**
```bash
curl http://localhost:9080/actuator/health
# Response: 404 Not Found
```

#### **Causa:**
Gateway `pom.xml` mancava la dipendenza `spring-boot-starter-actuator`

#### **Diagnosi Commands:**
```bash
# 1. Verifica dipendenze nel pom.xml
grep -A 5 -B 5 "actuator" gateway/initial/pom.xml

# 2. Check if actuator endpoints are exposed
grep "management.endpoints" gateway/initial/src/main/resources/application.properties

# 3. Test container startup logs
docker logs spring-mono_gateway_1 | grep -i actuator
```

#### **Soluzione:**
Aggiungere al `gateway/initial/pom.xml`:
```xml
<!-- Actuator for Health Endpoints (Phase 1 Security) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

### **4. 🔴 INCORRECT PORT MAPPING NEL DOCKER COMPOSE**

#### **Problema:**
PostgreSQL port mapping errato causava connection failures

#### **Causa:**
```yaml
# ERRATO:
ports:
  - "15432:15432"  # PostgreSQL internal port is 5432, not 15432

# CORRETTO:
ports:
  - "15432:5432"   # Host:Container port mapping
```

#### **Diagnosi Commands:**
```bash
# 1. Verifica porte container attive
docker ps --format "table {{.Names}}\t{{.Ports}}"

# 2. Test connessione PostgreSQL
docker exec spring-mono_postgres_1 pg_isready -U springuser

# 3. Check logs per connection errors
docker logs spring-mono_auth-service_1 | grep -i "connection\|postgres\|database"
```

#### **Soluzione:**
```bash
# Correggi port mapping nel docker-compose.staging.yml
sed -i 's/"15432:15432"/"15432:5432"/' docker-compose.staging.yml
```

---

### **5. 🔴 GATEWAY BUILD PATH INCORRECT**

#### **Problema:**
Docker non trovava il Dockerfile del gateway

#### **Causa:**
```yaml
# ERRATO:
build: ./gateway

# CORRETTO:
build: ./gateway/initial  # Path corretto con implementazione
```

#### **Diagnosi Commands:**
```bash
# 1. Verifica esistenza Dockerfile
ls -la gateway/Dockerfile
ls -la gateway/initial/Dockerfile

# 2. Check build context
docker-compose -f docker-compose.staging.yml config | grep -A 3 "gateway:"
```

#### **Soluzione:**
```bash
# Correggi build path
sed -i 's|build: ./gateway|build: ./gateway/initial|' docker-compose.staging.yml
```

---

## 🧪 TESTING COMMANDS SUITE

### **Quick Health Check:**
```bash
# Test all service health
curl -s http://localhost:9081/actuator/health | jq .
curl -s http://localhost:9082/actuator/health | jq .
curl -s http://localhost:9080/actuator/health | jq .
```

### **JWT Authentication Test:**
```bash
# 1. Login and get token
TOKEN=$(curl -s -X POST http://localhost:9081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

echo "Token: $TOKEN"

# 2. Test protected endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:9081/auth/me | jq .

# 3. Test token validation
curl -X POST http://localhost:9081/auth/validate \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}" | jq .
```

### **Gateway Routing Test:**
```bash
# Test gateway routing to auth-service
curl -X POST http://localhost:9080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

### **Rate Limiting Test:**
```bash
# Test rate limiting (should get 429 after limit)
for i in {1..10}; do
  response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9081/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"wrong"}')
  echo "Request $i: HTTP $response"
  sleep 0.1
done
```

---

## 🔍 DIAGNOSTIC COMMANDS

### **Container Status Check:**
```bash
# 1. All containers status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. Container resource usage
docker stats --no-stream

# 3. Container logs check
docker logs spring-mono_auth-service_1 --tail 50
docker logs spring-mono_gateway_1 --tail 50
docker logs spring-mono_postgres_1 --tail 20
```

### **Network Connectivity:**
```bash
# 1. Test inter-container connectivity
docker exec spring-mono_gateway_1 wget -qO- http://auth-service:8080/actuator/health
docker exec spring-mono_auth-service_1 wget -qO- http://postgres:5432

# 2. Port accessibility
nmap -p 9080,9081,9082,9443 localhost

# 3. Network inspect
docker network inspect spring-mono_default
```

### **Configuration Validation:**
```bash
# 1. Environment variables check
docker exec spring-mono_auth-service_1 env | grep -E "JWT_|SPRING_|POSTGRES_"
docker exec spring-mono_gateway_1 env | grep -E "JWT_|SSL_|REDIS_"

# 2. File system check
docker exec spring-mono_auth-service_1 find /app -name "*.class" | grep -E "(Auth|Jwt)"
docker exec spring-mono_gateway_1 find /app -name "*.class" | grep Gateway
```

---

## 🚀 COMPLETE FIX WORKFLOW

### **Step 1: Diagnose Issue**
```bash
# Quick issue identification
docker-compose -f docker-compose.staging.yml config  # YAML validation
docker ps | grep -E "(auth|gateway|postgres)"        # Container status
curl -I http://localhost:9081/auth/login              # Service response
```

### **Step 2: Apply Fixes**
```bash
# Fix Docker Compose YAML
cp docker-compose.staging.yml docker-compose.staging.yml.backup
sed -i 's/POSTGRES_USER: springuser      POSTGRES_PASSWORD:/POSTGRES_USER: springuser\n      POSTGRES_PASSWORD:/' docker-compose.staging.yml
sed -i 's/SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-change_me}      JWT_SECRET:/SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-change_me}\n      JWT_SECRET:/' docker-compose.staging.yml
sed -i 's/"15432:15432"/"15432:5432"/' docker-compose.staging.yml
sed -i 's|build: ./gateway|build: ./gateway/initial|' docker-compose.staging.yml
```

### **Step 3: Rebuild and Test**
```bash
# Complete rebuild
docker-compose -f docker-compose.staging.yml down
docker system prune -f
docker-compose -f docker-compose.staging.yml build --no-cache
docker-compose -f docker-compose.staging.yml up -d

# Wait for startup
sleep 30

# Test JWT functionality
curl -X POST http://localhost:9081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

---

## ⚡ EMERGENCY TROUBLESHOOTING

### **Container Won't Start:**
```bash
# Check specific container logs
docker logs spring-mono_auth-service_1 --details
docker logs spring-mono_gateway_1 --details

# Force recreate problematic container
docker-compose -f docker-compose.staging.yml up -d --force-recreate auth-service
```

### **Port Conflicts:**
```bash
# Find process using port
sudo netstat -tulpn | grep :9081
sudo lsof -i :9081

# Kill conflicting process
sudo fuser -k 9081/tcp
```

### **YAML Syntax Quick Fix:**
```bash
# Find all YAML syntax issues
python3 -c "import yaml; yaml.safe_load(open('docker-compose.staging.yml'))"

# Or use docker-compose validate
docker-compose -f docker-compose.staging.yml config --quiet
```

### **Database Connection Issues:**
```bash
# Test PostgreSQL connectivity
docker exec spring-mono_postgres_1 psql -U springuser -d mydb -c "SELECT version();"

# Reset database
docker-compose -f docker-compose.staging.yml restart postgres
docker logs spring-mono_postgres_1 --tail 20
```

---

## 📊 SUCCESS INDICATORS

### **✅ All Systems Working:**
```bash
# 1. All containers healthy
docker ps --filter "status=running" | grep -c spring-mono  # Should be 5

# 2. JWT authentication working
curl -s -X POST http://localhost:9081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token | wc -c  # Should be > 100

# 3. Gateway routing working
curl -s http://localhost:9080/actuator/health | jq -r .status  # Should be "UP"

# 4. All health endpoints responding
for port in 9080 9081 9082; do
  status=$(curl -s http://localhost:$port/actuator/health | jq -r .status 2>/dev/null || echo "FAIL")
  echo "Port $port: $status"
done
```

---

## 🎯 LESSONS LEARNED

### **1. Container vs Local Code Sync:**
- **Always rebuild containers** after code changes
- **Use `--no-cache`** for critical dependency changes
- **Verify file inclusion** in Docker images

### **2. YAML Syntax Sensitivity:**
- **One variable per line** in environment sections
- **Consistent indentation** (2 spaces)
- **Always validate** with `docker-compose config`

### **3. Dependency Management:**
- **Include all required dependencies** in pom.xml
- **Test actuator endpoints** explicitly
- **Verify starter dependencies** for Spring Boot features

### **4. Port Mapping Clarity:**
- **Host:Container** format understanding
- **Internal vs External** port distinction
- **Service name resolution** in Docker networks

---

## 📝 QUICK REFERENCE

### **Essential Commands:**
```bash
# Complete reset and rebuild
docker-compose -f docker-compose.staging.yml down && docker system prune -f && docker-compose -f docker-compose.staging.yml build --no-cache && docker-compose -f docker-compose.staging.yml up -d

# JWT test one-liner
curl -s -X POST http://localhost:9081/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | jq .

# Health check all services
for port in 9080 9081 9082; do echo "Port $port: $(curl -s http://localhost:$port/actuator/health | jq -r .status 2>/dev/null || echo 'FAIL')"; done

# Container logs monitoring
docker-compose -f docker-compose.staging.yml logs -f --tail 50
```

---

**Created**: May 30, 2025  
**Status**: Phase 1 JWT Security - RESOLVED  
**Next**: Phase 2 Advanced Security Features
