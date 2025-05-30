# 🧪 SECURITY TESTING GUIDE

## Phase 1 Security Features Testing

### Prerequisites
1. Start the services with Docker Compose:
   ```powershell
   cd "c:\Users\mini\Desktop\Visual code\microservices\spring-mono"
   docker-compose -f docker-compose.staging.yml up -d
   ```

2. Wait for services to be ready:
   ```powershell
   # Check health endpoints
   curl https://localhost:9443/actuator/health -k
   curl http://localhost:9081/actuator/health
   ```

---

## 🔐 1. JWT Authentication Testing

### Test 1: User Login
```powershell
# Test admin login
$loginResponse = Invoke-RestMethod -Uri "https://localhost:9443/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -SkipCertificateCheck
$token = $loginResponse.token
Write-Host "JWT Token: $token"
```

### Test 2: Protected Endpoint Access
```powershell
# Access protected endpoint with valid token
Invoke-RestMethod -Uri "https://localhost:9443/auth/me" -Method GET -Headers @{Authorization="Bearer $token"} -SkipCertificateCheck

# Access without token (should fail)
try {
    Invoke-RestMethod -Uri "https://localhost:9443/api/protected" -Method GET -SkipCertificateCheck
} catch {
    Write-Host "Expected 401 Unauthorized: $($_.Exception.Message)"
}
```

### Test 3: Token Validation
```powershell
# Validate token
Invoke-RestMethod -Uri "https://localhost:9443/auth/validate" -Method POST -ContentType "application/json" -Body "{\"token\":\"$token\"}" -SkipCertificateCheck
```

### Test 4: Invalid Credentials
```powershell
# Test with wrong password
try {
    Invoke-RestMethod -Uri "https://localhost:9443/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"wrongpassword"}' -SkipCertificateCheck
} catch {
    Write-Host "Expected 401 Unauthorized: $($_.Exception.Message)"
}
```

---

## 🔒 2. HTTPS/TLS Testing

### Test 1: HTTPS Enforcement
```powershell
# HTTPS should work (with self-signed certificate warning)
Invoke-RestMethod -Uri "https://localhost:9443/actuator/health" -SkipCertificateCheck

# HTTP should redirect to HTTPS
$response = Invoke-WebRequest -Uri "http://localhost:9080/actuator/health" -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "HTTP Response Status: $($response.StatusCode)" # Should be 301 or 302
```

### Test 2: Certificate Information
```powershell
# Check certificate details
openssl s_client -connect localhost:9443 -servername localhost < /dev/null 2>/dev/null | openssl x509 -text -noout
```

---

## 🚨 3. Rate Limiting Testing

### Test 1: Auth Endpoint Rate Limiting
```powershell
# Rapid login attempts (should trigger rate limiting)
for ($i = 1; $i -le 15; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "https://localhost:9443/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"test","password":"wrong"}' -SkipCertificateCheck
        Write-Host "Request $i: Success"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "Request $i: Rate limited (429 Too Many Requests)"
        } else {
            Write-Host "Request $i: Other error - $($_.Exception.Message)"
        }
    }
    Start-Sleep -Milliseconds 100
}
```

### Test 2: API Endpoint Rate Limiting
```powershell
# First get a valid token
$loginResponse = Invoke-RestMethod -Uri "https://localhost:9443/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -SkipCertificateCheck
$token = $loginResponse.token

# Rapid API requests
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "https://localhost:9443/auth/me" -Method GET -Headers @{Authorization="Bearer $token"} -SkipCertificateCheck
        Write-Host "API Request $i: Success"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "API Request $i: Rate limited (429)"
        } else {
            Write-Host "API Request $i: Error - $($_.Exception.Message)"
        }
    }
    Start-Sleep -Milliseconds 50
}
```

---

## 🗄️ 4. Database Security Testing

### Test 1: Environment Variables
```powershell
# Check that no hardcoded passwords are in configuration files
Select-String -Path "c:\Users\mini\Desktop\Visual code\microservices\spring-mono\*\*\*\application*.properties" -Pattern "password.*=" | Where-Object { $_.Line -notmatch '\$\{' }
```

### Test 2: Database Connection
```powershell
# Verify services can connect to database
docker logs microservices_auth-service_1 | Select-String -Pattern "DATABASE\|postgres\|connection"
```

---

## 🔧 5. Configuration Security Testing

### Test 1: Environment Variable Loading
```powershell
# Check environment variables are loaded properly
docker exec microservices_gateway_1 env | Select-String -Pattern "JWT_\|SSL_\|REDIS_"
```

### Test 2: Secret Protection
```powershell
# Verify .env file is not in git
git status --porcelain | Select-String -Pattern "\.env"

# Check .gitignore contains .env
Get-Content ".gitignore" | Select-String -Pattern "\.env"
```

---

## 📊 6. Service Health Checks

### Test 1: All Services Running
```powershell
# Check all services are healthy
$services = @("postgres", "redis", "auth-service", "chat-service", "gateway")
foreach ($service in $services) {
    $status = docker ps --filter "name=microservices_${service}_1" --format "table {{.Names}}\t{{.Status}}"
    Write-Host "$service status: $status"
}
```

### Test 2: Health Endpoints
```powershell
# Check health endpoints
$healthEndpoints = @(
    "https://localhost:9443/actuator/health",
    "http://localhost:9081/actuator/health",
    "http://localhost:9082/actuator/health"
)

foreach ($endpoint in $healthEndpoints) {
    try {
        $health = Invoke-RestMethod -Uri $endpoint -SkipCertificateCheck
        Write-Host "$endpoint : $($health.status)"
    } catch {
        Write-Host "$endpoint : ERROR - $($_.Exception.Message)"
    }
}
```

---

## 🐛 7. Security Vulnerability Testing

### Test 1: JWT Token Manipulation
```powershell
# Test with modified JWT token
$fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature"

try {
    Invoke-RestMethod -Uri "https://localhost:9443/auth/me" -Method GET -Headers @{Authorization="Bearer $fakeToken"} -SkipCertificateCheck
} catch {
    Write-Host "Expected JWT validation failure: $($_.Exception.Message)"
}
```

### Test 2: SQL Injection Protection
```powershell
# Test login with SQL injection attempts
$sqlPayloads = @(
    "admin'; DROP TABLE users; --",
    "admin' OR '1'='1",
    "admin' UNION SELECT * FROM users --"
)

foreach ($payload in $sqlPayloads) {
    try {
        Invoke-RestMethod -Uri "https://localhost:9443/auth/login" -Method POST -ContentType "application/json" -Body "{\"username\":\"$payload\",\"password\":\"test\"}" -SkipCertificateCheck
    } catch {
        Write-Host "SQL Injection test with '$payload': Properly rejected"
    }
}
```

---

## ✅ Expected Results

### ✅ Authentication Tests
- ✅ Valid credentials return JWT token
- ✅ Invalid credentials return 401 Unauthorized
- ✅ Protected endpoints require valid JWT
- ✅ Token validation works correctly

### ✅ HTTPS Tests
- ✅ HTTPS endpoints accessible with certificate warning
- ✅ HTTP requests redirect to HTTPS
- ✅ Self-signed certificate properly configured

### ✅ Rate Limiting Tests
- ✅ Auth endpoints limited to 5 requests/second
- ✅ API endpoints limited to 20 requests/second
- ✅ Rate limiting returns 429 status code
- ✅ Rate limits reset after time window

### ✅ Security Tests
- ✅ No hardcoded passwords in configuration
- ✅ Environment variables properly loaded
- ✅ Invalid JWT tokens rejected
- ✅ SQL injection attempts blocked

---

## 🚨 Troubleshooting

### Common Issues

1. **Certificate Errors**:
   ```powershell
   # Accept self-signed certificates for testing
   [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
   ```

2. **Service Not Ready**:
   ```powershell
   # Wait for services to start
   Start-Sleep -Seconds 30
   docker-compose logs gateway
   ```

3. **Rate Limiting Not Working**:
   ```powershell
   # Check Redis connection
   docker logs microservices_redis_1
   docker exec microservices_gateway_1 redis-cli -h redis ping
   ```

4. **JWT Issues**:
   ```powershell
   # Check JWT secret configuration
   docker exec microservices_auth-service_1 env | grep JWT_SECRET
   ```

---

**Testing Completed**: Run all tests to verify Phase 1 security implementation  
**Next Steps**: Begin Phase 2 security features implementation
