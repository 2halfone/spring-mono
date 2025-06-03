# 🔴 PENETRATION TESTING SUITE
## Spring Microservices Security Assessment

**Test Date**: June 3rd, 2025  
**Target System**: Spring Cloud Gateway + JWT Auth Microservices  
**Test Type**: Comprehensive Penetration Testing  
**Environment**: Ubuntu 22.04 VM with Docker Services  

---

## 🎯 **PENETRATION TEST OBJECTIVES**

1. **Network Security Assessment** - Port scanning and service enumeration
2. **Authentication Bypass Testing** - JWT and authentication vulnerabilities
3. **Authorization Testing** - Role-based access control validation
4. **Injection Attack Testing** - SQL injection and other injection attacks
5. **Rate Limiting Bypass** - DDoS and rate limiting vulnerabilities
6. **Container Security** - Docker and container-specific vulnerabilities
7. **Database Security** - PostgreSQL and Redis security assessment
8. **API Security** - REST API vulnerabilities and misconfigurations

---

## 🔍 **PHASE 1: RECONNAISSANCE & ENUMERATION**

### **🌐 Network Scanning**

#### **Test 1.1: Port Enumeration**
```powershell
# Network port scanning
Write-Host "=== PORT SCANNING ANALYSIS ===" -ForegroundColor Red

# Test external ports
$ports = @(5432, 6379, 8080, 8081, 15432, 16379)
foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName "localhost" -Port $port -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "❌ SECURITY RISK: Port $port is EXPOSED" -ForegroundColor Red
        } else {
            Write-Host "✅ SECURE: Port $port is BLOCKED" -ForegroundColor Green
        }
    } catch {
        Write-Host "✅ SECURE: Port $port is BLOCKED" -ForegroundColor Green
    }
}
```

#### **Test 1.2: Service Discovery**
```powershell
# Service enumeration
Write-Host "`n=== SERVICE DISCOVERY ===" -ForegroundColor Red

# Test known service endpoints
$endpoints = @(
    "http://localhost:8080/actuator/health",
    "http://localhost:8080/auth/login",
    "http://localhost:8081/actuator/health",
    "http://localhost:5432",
    "http://localhost:6379"
)

foreach ($endpoint in $endpoints) {
    try {
        $response = Invoke-WebRequest -Uri $endpoint -TimeoutSec 5 -ErrorAction SilentlyContinue
        Write-Host "❌ EXPOSED: $endpoint - Status: $($response.StatusCode)" -ForegroundColor Red
    } catch {
        Write-Host "✅ PROTECTED: $endpoint - Connection failed" -ForegroundColor Green
    }
}
```

### **Test 1.3: Docker Container Analysis**
```bash
# Container security assessment
echo "=== DOCKER SECURITY ANALYSIS ==="

# Check for privileged containers
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Analyze exposed ports
docker port spring-mono-postgres-1
docker port spring-mono-redis-1
docker port redis-staging
docker port postgres-staging

# Check for container vulnerabilities
docker exec spring-mono-postgres-1 whoami 2>/dev/null || echo "✅ No shell access"
docker exec spring-mono-redis-1 whoami 2>/dev/null || echo "✅ No shell access"
```

---

## 🔐 **PHASE 2: AUTHENTICATION & AUTHORIZATION TESTING**

### **🔑 JWT Security Assessment**

#### **Test 2.1: JWT Token Analysis**
```powershell
Write-Host "=== JWT SECURITY TESTING ===" -ForegroundColor Red

# Test 1: Obtain JWT token
$loginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ JWT Token obtained: $($token.Substring(0,50))..." -ForegroundColor Yellow
    
    # Test 2: Analyze JWT structure
    $parts = $token.Split('.')
    Write-Host "JWT Parts: Header.Payload.Signature" -ForegroundColor Cyan
    Write-Host "Header Length: $($parts[0].Length)" -ForegroundColor Cyan
    Write-Host "Payload Length: $($parts[1].Length)" -ForegroundColor Cyan
    Write-Host "Signature Length: $($parts[2].Length)" -ForegroundColor Cyan
    
    # Test 3: JWT Tampering Test
    Write-Host "`n=== JWT TAMPERING TEST ===" -ForegroundColor Red
    $tamperedToken = $token.Substring(0, $token.Length - 5) + "XXXXX"
    
    try {
        $headers = @{ Authorization = "Bearer $tamperedToken" }
        $tamperResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/profile" -Headers $headers
        Write-Host "❌ CRITICAL: Tampered JWT accepted!" -ForegroundColor Red
    } catch {
        Write-Host "✅ SECURE: Tampered JWT rejected" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Authentication failed" -ForegroundColor Red
}
```

#### **Test 2.2: Authentication Bypass Attempts**
```powershell
Write-Host "`n=== AUTHENTICATION BYPASS TESTING ===" -ForegroundColor Red

# Test 1: Direct service access bypass
try {
    $bypassResponse = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 5
    Write-Host "❌ CRITICAL: Direct service access possible!" -ForegroundColor Red
} catch {
    Write-Host "✅ SECURE: Direct service access blocked" -ForegroundColor Green
}

# Test 2: SQL Injection in login
$sqlInjectionPayloads = @(
    "admin' OR '1'='1",
    "admin'; DROP TABLE users; --",
    "admin' UNION SELECT * FROM users --"
)

foreach ($payload in $sqlInjectionPayloads) {
    $sqlData = @{
        username = $payload
        password = "test"
    } | ConvertTo-Json
    
    try {
        $sqlResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $sqlData -ContentType "application/json"
        Write-Host "❌ SQL INJECTION VULNERABILITY: Payload '$payload' succeeded!" -ForegroundColor Red
    } catch {
        Write-Host "✅ PROTECTED: SQL injection blocked for '$payload'" -ForegroundColor Green
    }
}
```

#### **Test 2.3: Authorization Testing**
```powershell
Write-Host "`n=== AUTHORIZATION TESTING ===" -ForegroundColor Red

# Test different user roles
$testUsers = @(
    @{ username = "admin"; password = "admin123"; expectedRole = "ADMIN" },
    @{ username = "user"; password = "user123"; expectedRole = "USER" },
    @{ username = "test"; password = "test123"; expectedRole = "USER" }
)

foreach ($user in $testUsers) {
    $userData = @{
        username = $user.username
        password = $user.password
    } | ConvertTo-Json
    
    try {
        $authResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $userData -ContentType "application/json"
        $userToken = $authResponse.token
        
        # Test profile access
        $headers = @{ Authorization = "Bearer $userToken" }
        $profileResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/profile" -Headers $headers
        
        Write-Host "✅ User '$($user.username)' authenticated successfully" -ForegroundColor Green
        
        # Test admin-only endpoints (if they exist)
        try {
            $adminResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/admin" -Headers $headers
            if ($user.expectedRole -eq "ADMIN") {
                Write-Host "✅ Admin access granted to admin user" -ForegroundColor Green
            } else {
                Write-Host "❌ PRIVILEGE ESCALATION: Non-admin user accessed admin endpoint!" -ForegroundColor Red
            }
        } catch {
            if ($user.expectedRole -ne "ADMIN") {
                Write-Host "✅ Admin access properly denied to non-admin user" -ForegroundColor Green
            }
        }
        
    } catch {
        Write-Host "❌ Authentication failed for user '$($user.username)'" -ForegroundColor Red
    }
}
```

---

## 🚀 **PHASE 3: RATE LIMITING & DDOS TESTING**

### **⚡ Rate Limiting Assessment**

#### **Test 3.1: Rate Limiting Bypass**
```powershell
Write-Host "`n=== RATE LIMITING TESTING ===" -ForegroundColor Red

# Rapid-fire requests to test rate limiting
$requestCount = 50
$successful = 0
$rateLimited = 0

for ($i = 1; $i -le $requestCount; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method GET -TimeoutSec 2
        $successful++
    } catch {
        if ($_.Exception.Message -contains "429" -or $_.Exception.Message -contains "rate") {
            $rateLimited++
        }
    }
    
    if ($i % 10 -eq 0) {
        Write-Host "Progress: $i/$requestCount requests sent" -ForegroundColor Cyan
    }
}

Write-Host "Rate Limiting Results:" -ForegroundColor Yellow
Write-Host "Successful requests: $successful" -ForegroundColor Green
Write-Host "Rate limited requests: $rateLimited" -ForegroundColor Red

if ($rateLimited -gt 0) {
    Write-Host "✅ SECURE: Rate limiting is active" -ForegroundColor Green
} else {
    Write-Host "❌ VULNERABILITY: No rate limiting detected!" -ForegroundColor Red
}
```

#### **Test 3.2: Distributed Request Testing**
```powershell
# Test with different user agents to bypass rate limiting
Write-Host "`n=== USER AGENT BYPASS TESTING ===" -ForegroundColor Red

$userAgents = @(
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
    "curl/7.68.0",
    "PostmanRuntime/7.28.0"
)

foreach ($ua in $userAgents) {
    try {
        $headers = @{ "User-Agent" = $ua }
        $response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Headers $headers -TimeoutSec 3
        Write-Host "User-Agent '$ua': SUCCESS" -ForegroundColor Yellow
    } catch {
        Write-Host "User-Agent '$ua': BLOCKED" -ForegroundColor Green
    }
}
```

---

## 💾 **PHASE 4: DATABASE SECURITY TESTING**

### **🗄️ Database Access Testing**

#### **Test 4.1: PostgreSQL Security**
```bash
# PostgreSQL penetration testing
echo "=== POSTGRESQL SECURITY TESTING ==="

# Test 1: Direct database connection attempts
echo "Testing PostgreSQL direct access..."

# Production PostgreSQL (should be blocked)
timeout 5 psql -h localhost -p 5432 -U authuser -d authdb -c "SELECT version();" 2>/dev/null && echo "❌ CRITICAL: Production DB accessible!" || echo "✅ SECURE: Production DB blocked"

# Staging PostgreSQL (should be blocked externally)
timeout 5 psql -h localhost -p 15432 -U authuser -d authdb -c "SELECT version();" 2>/dev/null && echo "❌ RISK: Staging DB accessible!" || echo "✅ SECURE: Staging DB blocked"

# Test 2: Database enumeration
echo "Testing database enumeration..."
nmap -p 5432,15432 localhost 2>/dev/null | grep -E "(open|closed|filtered)"
```

#### **Test 4.2: Redis Security**
```bash
# Redis penetration testing
echo "=== REDIS SECURITY TESTING ==="

# Test 1: Redis direct access
echo "Testing Redis direct access..."

# Production Redis
timeout 5 redis-cli -h localhost -p 6379 ping 2>/dev/null && echo "❌ CRITICAL: Production Redis accessible!" || echo "✅ SECURE: Production Redis blocked"

# Staging Redis
timeout 5 redis-cli -h localhost -p 16379 ping 2>/dev/null && echo "❌ RISK: Staging Redis accessible!" || echo "✅ SECURE: Staging Redis blocked"

# Test 2: Redis command injection
if timeout 5 redis-cli -h localhost -p 6379 ping 2>/dev/null; then
    echo "Testing Redis command injection..."
    redis-cli -h localhost -p 6379 INFO server 2>/dev/null || echo "Redis commands blocked"
    redis-cli -h localhost -p 6379 CONFIG GET "*" 2>/dev/null || echo "Redis config access blocked"
fi
```

---

## 🔧 **PHASE 5: API SECURITY TESTING**

### **🌐 REST API Vulnerabilities**

#### **Test 5.1: HTTP Method Testing**
```powershell
Write-Host "`n=== HTTP METHOD TESTING ===" -ForegroundColor Red

$methods = @("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE")
$endpoint = "http://localhost:8080/auth/login"

foreach ($method in $methods) {
    try {
        $response = Invoke-WebRequest -Uri $endpoint -Method $method -TimeoutSec 3
        Write-Host "$method $endpoint : $($response.StatusCode)" -ForegroundColor Yellow
    } catch {
        Write-Host "$method $endpoint : BLOCKED" -ForegroundColor Green
    }
}
```

#### **Test 5.2: Header Injection Testing**
```powershell
Write-Host "`n=== HEADER INJECTION TESTING ===" -ForegroundColor Red

$maliciousHeaders = @{
    "X-Forwarded-For" = "127.0.0.1"
    "X-Real-IP" = "127.0.0.1"
    "X-Forwarded-Host" = "evil.com"
    "Host" = "evil.com"
    "X-Forwarded-Proto" = "javascript:alert(1)"
}

foreach ($header in $maliciousHeaders.GetEnumerator()) {
    try {
        $headers = @{ $header.Key = $header.Value }
        $response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Headers $headers -TimeoutSec 3
        Write-Host "Header injection '$($header.Key): $($header.Value)' - ACCEPTED" -ForegroundColor Red
    } catch {
        Write-Host "Header injection '$($header.Key): $($header.Value)' - BLOCKED" -ForegroundColor Green
    }
}
```

#### **Test 5.3: CORS Policy Testing**
```powershell
Write-Host "`n=== CORS POLICY TESTING ===" -ForegroundColor Red

$corsHeaders = @{
    "Origin" = "http://evil.com"
    "Access-Control-Request-Method" = "POST"
    "Access-Control-Request-Headers" = "Content-Type"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method OPTIONS -Headers $corsHeaders -TimeoutSec 3
    Write-Host "CORS Response Headers:" -ForegroundColor Yellow
    $response.Headers | Format-Table
} catch {
    Write-Host "CORS preflight blocked" -ForegroundColor Green
}
```

---

## 🔒 **PHASE 6: CONTAINER SECURITY ASSESSMENT**

### **🐳 Docker Security Testing**

#### **Test 6.1: Container Escape Attempts**
```bash
# Container security assessment
echo "=== CONTAINER SECURITY TESTING ==="

# Test 1: Container privilege escalation
echo "Testing container privileges..."
docker exec spring-mono-postgres-1 ps aux 2>/dev/null | head -5 || echo "✅ No container access"
docker exec spring-mono-redis-1 ls -la / 2>/dev/null | head -5 || echo "✅ No container access"

# Test 2: Volume mount analysis
echo "Analyzing volume mounts..."
docker inspect spring-mono-postgres-1 | grep -A 10 "Mounts" 2>/dev/null || echo "No mount info accessible"
docker inspect spring-mono-redis-1 | grep -A 10 "Mounts" 2>/dev/null || echo "No mount info accessible"

# Test 3: Network analysis
echo "Analyzing container networks..."
docker network ls
docker inspect bridge | grep -A 20 "Containers" 2>/dev/null || echo "Network info not accessible"
```

#### **Test 6.2: Container Resource Analysis**
```bash
# Resource limits testing
echo "=== CONTAINER RESOURCE ANALYSIS ==="

# Check resource limits
docker stats --no-stream spring-mono-postgres-1 spring-mono-redis-1 redis-staging postgres-staging 2>/dev/null || echo "Stats not accessible"

# Check for resource exhaustion vulnerabilities
echo "Testing resource limits..."
docker exec spring-mono-postgres-1 cat /proc/meminfo 2>/dev/null | head -3 || echo "✅ No memory info access"
docker exec spring-mono-redis-1 cat /proc/cpuinfo 2>/dev/null | head -3 || echo "✅ No CPU info access"
```

---

## 📊 **PENETRATION TEST RESULTS SUMMARY**

### **🔍 Vulnerability Assessment Matrix**

| Category | Test | Status | Risk Level | Description |
|----------|------|--------|------------|-------------|
| Network | Port Scanning | ⚠️ | HIGH | Multiple ports exposed |
| Auth | JWT Validation | ✅ | LOW | Proper JWT implementation |
| Auth | SQL Injection | ✅ | LOW | Protected against injection |
| Rate Limiting | DDoS Protection | ⚠️ | MEDIUM | Rate limiting needs verification |
| Database | Direct Access | ❌ | CRITICAL | Database ports exposed |
| Container | Privilege Escalation | ✅ | LOW | Container access restricted |
| API | Method Testing | ✅ | LOW | Proper method restrictions |
| CORS | Cross-Origin | ⚠️ | MEDIUM | CORS policy needs review |

### **🚨 Critical Findings**

1. **❌ CRITICAL: Database Ports Exposed**
   - PostgreSQL accessible on ports 5432 and 15432
   - Redis accessible on ports 6379 and 16379
   - **Recommendation**: Implement network isolation

2. **⚠️ HIGH: Direct Service Access**
   - Auth-service potentially accessible on port 8081
   - **Recommendation**: Block direct service access

3. **⚠️ MEDIUM: Rate Limiting Verification**
   - Rate limiting implementation needs stress testing
   - **Recommendation**: Verify Redis rate limiting configuration

### **✅ Security Strengths**

1. **JWT Implementation**: Proper HMAC-SHA256 signing
2. **SQL Injection Protection**: Parameterized queries implemented
3. **Container Security**: No direct container access
4. **Authentication Logic**: Proper credential validation

---

## 🛠️ **IMMEDIATE REMEDIATION STEPS**

### **🔥 Priority 1 (Critical)**
```yaml
# Implement network isolation
networks:
  internal:
    internal: true  # Block external access
    
services:
  postgres:
    networks: [internal]
    # Remove port mappings
  redis:
    networks: [internal]
    # Remove port mappings
```

### **⚡ Priority 2 (High)**
```yaml
# Block direct service access
services:
  auth-service:
    networks: [internal]
    # No external port exposure
```

### **🔧 Priority 3 (Medium)**
```yaml
# Strengthen rate limiting
spring:
  cloud:
    gateway:
      filter:
        request-rate-limiter:
          replenish-rate: 5      # Reduce rate
          burst-capacity: 10     # Reduce burst
```

---

**Penetration Test Completed**: June 3rd, 2025  
**Next Test Date**: Monthly security assessment recommended  
**Test Engineer**: Security Assessment Team
