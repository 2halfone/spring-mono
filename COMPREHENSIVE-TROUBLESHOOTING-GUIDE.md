# 🔧 Guida Completa al Troubleshooting Spring Microservices

## 📋 Indice
1. [Panoramica dei Problemi](#panoramica-dei-problemi)
2. [Problema 1: JWT Files Missing in VM](#problema-1-jwt-files-missing-in-vm)
3. [Problema 2: Docker Compose YAML Syntax Errors](#problema-2-docker-compose-yaml-syntax-errors)
4. [Problema 3: PostgreSQL Port Mapping](#problema-3-postgresql-port-mapping)
5. [Problema 4: Gateway Build Path](#problema-4-gateway-build-path)
6. [Problema 5: Missing Actuator Dependency](#problema-5-missing-actuator-dependency)
7. [Problema 6: Container Caching Issues](#problema-6-container-caching-issues)
8. [Comandi di Troubleshooting](#comandi-di-troubleshooting)
9. [Validazione Post-Fix](#validazione-post-fix)
10. [Checklist di Verifica](#checklist-di-verifica)

---

## 🎯 Panoramica dei Problemi

Durante l'implementazione delle security measures per Spring Microservices, abbiamo identificato e risolto **6 problemi critici** che impedivano il corretto funzionamento del sistema:

### Problemi Identificati:
- ❌ JWT Authentication files apparentemente mancanti in VM
- ❌ Errori di sintassi YAML in docker-compose.staging.yml
- ❌ Port mapping PostgreSQL errato
- ❌ Gateway build path scorretto
- ❌ Dipendenza Actuator mancante nel Gateway
- ❌ Docker container caching problemi

### Stato Finale:
- ✅ JWT Authentication completamente funzionante
- ✅ Docker Compose sintassi corretta
- ✅ Tutti i servizi operativi
- ✅ Container health verificato

---

## 🚨 Problema 1: JWT Files Missing in VM

### **Descrizione**
Durante i test VM, sembrava che i file JWT fossero mancanti, causando errori di compilazione.

### **Root Cause Analysis**
```powershell
# Comando per verificare la presenza dei file JWT
Get-ChildItem -Path "c:\Users\mini\Desktop\Visual code\microservices\spring-mono" -Recurse -Name "*jwt*" -Force
Get-ChildItem -Path "c:\Users\mini\Desktop\Visual code\microservices\spring-mono" -Recurse -Name "*Auth*" -Force
```

### **Diagnosi**
I file JWT erano **presenti localmente** ma non venivano inclusi nei Docker builds a causa di:
1. Docker build context issues
2. Container caching di versioni precedenti
3. Build path errati

### **Files Verificati Presenti:**
```
auth-service\src\main\java\com\example\controller\AuthController.java
auth-service\src\main\java\com\example\service\AuthService.java
auth-service\src\main\java\com\example\security\JwtUtil.java
auth-service\src\main\java\com\example\dto\LoginRequest.java
auth-service\src\main\java\com\example\dto\JwtResponse.java
gateway\initial\src\main\java\com\example\gateway\JwtAuthenticationGatewayFilterFactory.java
gateway\initial\src\main\java\com\example\security\JwtUtil.java
```

### **Soluzione**
```powershell
# 1. Verificare presenza file
ls auth-service\src\main\java\com\example\controller\AuthController.java
ls auth-service\src\main\java\com\example\service\AuthService.java

# 2. Rebuild completo container
docker-compose -f docker-compose.staging.yml down --volumes --remove-orphans
docker system prune -f
docker-compose -f docker-compose.staging.yml build --no-cache
```

---

## 🚨 Problema 2: Docker Compose YAML Syntax Errors

### **Descrizione**
Errori di sintassi YAML critici in `docker-compose.staging.yml` che impedivano il deployment.

### **Diagnosi Command**
```powershell
# Validare sintassi YAML
docker-compose -f docker-compose.staging.yml config --quiet
```

### **Errori Identificati:**

#### **Linea 9 - PostgreSQL Environment Variables**
```yaml
# ❌ ERRATO (linea 9)
POSTGRES_USER: postgres POSTGRES_PASSWORD: postgres

# ✅ CORRETTO
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
```

#### **Linea 48 - Auth Service Environment Variables**
```yaml
# ❌ ERRATO (linea 48)
SPRING_DATASOURCE_PASSWORD: postgres JWT_SECRET: mySecretKey

# ✅ CORRETTO
SPRING_DATASOURCE_PASSWORD: postgres
JWT_SECRET: mySecretKey
```

### **Fix Commands**
```powershell
# 1. Backup del file originale
Copy-Item docker-compose.staging.yml docker-compose.staging.yml.backup

# 2. Validare fix
docker-compose -f docker-compose.staging.yml config --quiet

# 3. Se nessun output = sintassi corretta
echo "YAML syntax is valid"
```

---

## 🚨 Problema 3: PostgreSQL Port Mapping

### **Descrizione**
Port mapping PostgreSQL errato causava connection failures.

### **Errore Identificato:**
```yaml
# ❌ ERRATO
ports:
  - "15432:15432"  # PostgreSQL non ascolta su porta 15432 internamente

# ✅ CORRETTO  
ports:
  - "15432:5432"   # Container PostgreSQL ascolta sempre su porta 5432
```

### **Diagnosi Commands**
```powershell
# Verificare porte in uso
docker-compose -f docker-compose.staging.yml ps
netstat -an | findstr :15432

# Testare connessione PostgreSQL
docker exec postgres_staging pg_isready -h localhost -p 5432
```

### **Validazione Connection**
```powershell
# Test connessione database
docker exec -it postgres_staging psql -U postgres -d microservices_db -c "\dt"
```

---

## 🚨 Problema 4: Gateway Build Path

### **Descrizione**
Docker build path errato per il Gateway service.

### **Errore Identificato:**
```yaml
# ❌ ERRATO in docker-compose.staging.yml
gateway:
  build: 
    context: ./gateway  # Path errato, manca /initial

# ✅ CORRETTO
gateway:
  build: 
    context: ./gateway/initial  # Path corretto
```

### **Diagnosi Commands**
```powershell
# Verificare struttura directory
Get-ChildItem -Path "gateway" -Recurse -Name "Dockerfile"

# Output atteso:
# initial\Dockerfile
# complete\Dockerfile (se presente)

# Verificare Dockerfile nel path corretto
Test-Path "gateway\initial\Dockerfile"
```

---

## 🚨 Problema 5: Missing Actuator Dependency

### **Descrizione**
Dipendenza Spring Boot Actuator mancante nel Gateway, necessaria per health endpoints.

### **Diagnosi**
```powershell
# Verificare dipendenze nel pom.xml
Select-String -Path "gateway\initial\pom.xml" -Pattern "actuator"
```

### **Fix Implementato**
Aggiunta dipendenza in `gateway/initial/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### **Validazione**
```powershell
# Test health endpoint dopo deployment
curl http://localhost:9080/actuator/health
```

---

## 🚨 Problema 6: Container Caching Issues

### **Descrizione**
Docker utilizzava container cached con versioni outdated dei file.

### **Diagnosi Commands**
```powershell
# Verificare immagini Docker esistenti
docker images | findstr "spring-mono"

# Verificare container in esecuzione
docker ps -a
```

### **Soluzione Completa**
```powershell
# 1. Stop completo e cleanup
docker-compose -f docker-compose.staging.yml down --volumes --remove-orphans

# 2. Rimuovere immagini cached
docker rmi $(docker images "spring-mono*" -q) --force

# 3. System cleanup
docker system prune -f

# 4. Rebuild completo senza cache
docker-compose -f docker-compose.staging.yml build --no-cache

# 5. Deploy
docker-compose -f docker-compose.staging.yml up -d
```

---

## 🛠️ Comandi di Troubleshooting

### **Diagnosi Rapida del Sistema**
```powershell
# 1. Status generale containers
docker-compose -f docker-compose.staging.yml ps

# 2. Log di tutti i servizi
docker-compose -f docker-compose.staging.yml logs --tail=50

# 3. Health check specifico per servizio
docker-compose -f docker-compose.staging.yml exec auth-service curl http://localhost:8080/actuator/health
docker-compose -f docker-compose.staging.yml exec gateway curl http://localhost:8080/actuator/health
```

### **Diagnosi Connettività Database**
```powershell
# 1. Test connessione PostgreSQL
docker exec postgres_staging pg_isready -h localhost -p 5432

# 2. Verificare database e tabelle
docker exec -it postgres_staging psql -U postgres -d microservices_db -c "\dt"

# 3. Test connessione da auth-service
docker-compose -f docker-compose.staging.yml exec auth-service curl http://localhost:8080/actuator/health
```

### **Diagnosi JWT Authentication**
```powershell
# 1. Test login endpoint
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "http://localhost:9081/auth/login" -Method Post -Body $body -Headers $headers

# 2. Test validate endpoint (con token ottenuto)
$token = "YOUR_JWT_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:9081/auth/validate" -Method Get -Headers $headers
```

### **Diagnosi Gateway Routing**
```powershell
# 1. Test routing via Gateway
curl http://localhost:9080/auth/actuator/health

# 2. Verificare route configuration
docker-compose -f docker-compose.staging.yml exec gateway curl http://localhost:8080/actuator/gateway/routes
```

### **Diagnosi Rate Limiting**
```powershell
# 1. Verificare Redis connessione
docker exec redis_staging redis-cli ping

# 2. Test rate limiting (multiple requests)
for ($i = 1; $i -le 10; $i++) {
    curl http://localhost:9080/auth/actuator/health
    Write-Host "Request $i completed"
}
```

---

## ✅ Validazione Post-Fix

### **Test Suite Completo**
```powershell
# 1. Container Health
docker-compose -f docker-compose.staging.yml ps
echo "All containers should show 'Up' status"

# 2. Database Connectivity
docker exec postgres_staging pg_isready -h localhost -p 5432
echo "PostgreSQL should respond 'accepting connections'"

# 3. JWT Authentication Test
$loginTest = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:9081/auth/login" -Method Post -Body $loginTest -Headers @{"Content-Type"="application/json"}
echo "JWT Token received: $($response.token.Substring(0,20))..."

# 4. Gateway Health
curl http://localhost:9080/actuator/health
echo "Gateway health endpoint should return status UP"

# 5. Redis Connectivity  
docker exec redis_staging redis-cli ping
echo "Redis should respond PONG"
```

### **Success Criteria**
- ✅ Tutti i container in stato "Up"
- ✅ PostgreSQL accepting connections
- ✅ JWT login restituisce token valido
- ✅ Gateway health endpoint accessibile
- ✅ Redis responds to ping

---

## 📋 Checklist di Verifica

### **Pre-Deployment Checklist**
- [ ] YAML syntax validated con `docker-compose config`
- [ ] Tutti i Dockerfile presenti nei path corretti
- [ ] Dependencies verificate nei pom.xml
- [ ] Environment variables correttamente formattate
- [ ] Port mappings verificati

### **Post-Deployment Checklist**
- [ ] Container status: tutti "Up"
- [ ] Database connectivity: PostgreSQL ready
- [ ] JWT Authentication: login funzionante
- [ ] Gateway routing: endpoints accessibili
- [ ] Rate limiting: Redis operativo
- [ ] Health endpoints: tutti rispondono

### **Emergency Troubleshooting**
```powershell
# Quick Reset (in caso di problemi critici)
docker-compose -f docker-compose.staging.yml down --volumes --remove-orphans
docker system prune -f
docker-compose -f docker-compose.staging.yml build --no-cache
docker-compose -f docker-compose.staging.yml up -d

# Attendere 30 secondi per startup completo
Start-Sleep -Seconds 30

# Verificare stato
docker-compose -f docker-compose.staging.yml ps
```

---

## 🎯 Risultati Finali

### **Prima dei Fix:**
- ❌ JWT Authentication non funzionante
- ❌ Container failing to start
- ❌ Database connection errors
- ❌ Gateway routing issues

### **Dopo i Fix:**
- ✅ JWT Authentication completamente operativo
- ✅ Tutti i container healthy
- ✅ Database connectivity stabile
- ✅ Gateway routing funzionante
- ✅ Rate limiting Redis operativo

### **JWT Test Success Example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIs...",
  "type": "Bearer",
  "username": "admin", 
  "roles": "ADMIN,USER",
  "expiresIn": 86400
}
```

---

## 📞 Supporto

Per ulteriori problemi o domande:
1. Consultare i log: `docker-compose -f docker-compose.staging.yml logs [service-name]`
2. Verificare network: `docker network ls`
3. Reset completo: seguire Emergency Troubleshooting section

---

**Documento creato:** Maggio 2025  
**Versione:** 1.0  
**Status:** ✅ SISTEMA OPERATIVO E VALIDATO
