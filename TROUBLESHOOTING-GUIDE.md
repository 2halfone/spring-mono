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

---

## 🚨 PARTE 2: PROBLEMI JWT 403 FORBIDDEN

### **6. 🔴 JWT TOKEN VALIDO MA 403 FORBIDDEN SU /auth/me**

#### **Problema Rilevato:**
```bash
# Token JWT ottenuto con successo dal login
TOKEN="eyJhbGciOiJIUzI1NiJ9...cso8DI1eYeNyJOUnWdADzvxGxfAkuVMtWx4x02729lc"

# Ma endpoint /auth/me restituisce 403 Forbidden
curl -H "Authorization: Bearer $TOKEN" http://localhost:9081/auth/me -v
# Response: HTTP 403 Forbidden
# Headers di sicurezza presenti ma content-length = 0
```

#### **Causa Possibili:**
1. **JWT Filter non configurato** correttamente per endpoint `/auth/me`
2. **Security Configuration** blocca l'endpoint nonostante token valido
3. **Role-based access** non implementato per endpoint protetti
4. **Gateway JWT Filter** non sta forwardando correttamente l'autorizzazione

#### **Diagnosi Commands:**
```bash
# 1. Verifica logs auth-service per errori JWT
docker logs spring-mono_auth-service_1 --tail 50 | grep -i -E "(jwt|auth|403|forbidden)"

# 2. Test diretto su auth-service (bypass gateway)
curl -H "Authorization: Bearer $TOKEN" http://localhost:9081/auth/me -v

# 3. Verifica implementazione endpoint /auth/me
docker exec spring-mono_auth-service_1 find /app -name "AuthController.class"

# 4. Test con altri endpoint protetti
curl -H "Authorization: Bearer $TOKEN" http://localhost:9081/auth/validate -v
```

#### **Possibili Soluzioni:**
##### **Soluzione 1: Verifica AuthController.java**
```java
// Verificare che l'endpoint /auth/me sia implementato
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // Implementazione mancante o errata
    }
}
```

##### **Soluzione 2: Security Configuration**
```java
// Verificare SecurityConfig per permettere endpoint autenticati
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/login", "/auth/validate").permitAll()
            .requestMatchers("/auth/me").authenticated()  // DEVE essere authenticated, non permitAll
            .anyRequest().authenticated()
        );
    }
}
```

##### **Soluzione 3: JWT Filter Chain**
```bash
# Verifica che JWT filter sia nella chain corretta
docker logs spring-mono_auth-service_1 | grep -i "filter"
```

---

### **7. 🔴 RATE LIMITING NON FUNZIONANTE**

#### **Problema Rilevato:**
```bash
# Rate limiting test mostra tutti 403 invece di progressione 200 -> 429
for i in {1..10}; do
  curl -s -o /dev/null -w "Request $i: %{http_code}\n" http://localhost:9081/auth/me \
    -H "Authorization: Bearer $TOKEN"
done

# Output: Tutti Request X: 403 (dovrebbe essere 200, 200, 200... poi 429)
```

#### **Causa:**
Il **403 Forbidden** impedisce di testare correttamente il rate limiting perché le richieste vengono bloccate prima di raggiungere il rate limiter.

#### **Diagnosi Commands:**
```bash
# 1. Verifica Redis per rate limiting
docker exec spring-mono_redis_1 redis-cli ping
docker exec spring-mono_redis_1 redis-cli keys "*rate*"

# 2. Test rate limiting su endpoint funzionante (login)
for i in {1..5}; do
  response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9081/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"wrong"}')
  echo "Request $i: HTTP $response"
  sleep 0.1
done

# 3. Verifica configurazione rate limiting
docker logs spring-mono_auth-service_1 | grep -i "redis\|rate"
```

---

## 🔧 IMMEDIATE FIX ACTIONS

### **Step 1: Verifica Implementazione /auth/me**
```bash
# 1. Controlla se AuthController ha il metodo /me
docker exec spring-mono_auth-service_1 find /app -name "*.class" | grep Auth

# 2. Verifica logs durante test
docker logs spring-mono_auth-service_1 --tail 20 &
curl -H "Authorization: Bearer $TOKEN" http://localhost:9081/auth/me
```

### **Step 2: Test Endpoint Alternativi**
```bash
# Test /auth/validate che dovrebbe funzionare
curl -X POST http://localhost:9081/auth/validate \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}"

# Se validate funziona, il problema è specifico di /auth/me
```

### **Step 3: Rebuild se Necessario**
```bash
# Se il problema è nel codice, rebuild
docker-compose -f docker-compose.staging.yml restart auth-service
docker logs spring-mono_auth-service_1 --tail 30
```

---

## 📊 STATUS CHECK AGGIORNATO

### **✅ Funzionante:**
- JWT Login: ✅ **200 OK** - Token generato correttamente
- Container Health: ✅ **UP** - Tutti i servizi attivi
- Database: ✅ **Connected** - PostgreSQL operativo
- YAML Syntax: ✅ **Valid** - Docker compose corretto

### **❌ Da Risolvere:**
- JWT /auth/me: ❌ **403 Forbidden** - Endpoint protetto non accessibile
- Rate Limiting: ❌ **Non testabile** - Bloccato da 403 su endpoints protetti
- Gateway JWT: ❓ **Da verificare** - Possibile problema nel forwarding

### **🔄 Test Priority:**
1. **Priorità 1**: Fix /auth/me endpoint (403 → 200)
2. **Priorità 2**: Verifica altri endpoints protetti
3. **Priorità 3**: Test rate limiting su endpoints funzionanti
4. **Priorità 4**: Gateway JWT routing validation

---

**Updated**: May 30, 2025 - 21:35 UTC  
**VM Test Status**: JWT Login ✅ | Protected Endpoints ❌  
**Next Action**: Investigate AuthController /me endpoint implementation
