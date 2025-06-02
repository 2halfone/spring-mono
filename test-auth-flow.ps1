# 🧪 SCRIPT TEST FLUSSO AUTENTICAZIONE REALE

# Configurazione Test
$baseUrl = "http://localhost:8080"  # Gateway HTTP port
$authUrl = "$baseUrl/auth"

Write-Host "🔐 TESTING AUTHENTICATION FLOW - REAL SYSTEM" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Test 1: Health Check Gateway
Write-Host "`n📊 TEST 1: Gateway Health Check" -ForegroundColor Yellow
try {
    $healthResponse = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -UseBasicParsing
    Write-Host "✅ Gateway Health: $($healthResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Content: $($healthResponse.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Gateway Health Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: User Registration
Write-Host "`n👤 TEST 2: User Registration" -ForegroundColor Yellow
$registrationData = @{
    username = "testuser$(Get-Random -Minimum 100 -Maximum 999)"
    password = "SecurePass123!"
    email = "test@example.com"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-WebRequest -Uri "$authUrl/register" -Method POST -Body $registrationData -ContentType "application/json" -UseBasicParsing
    Write-Host "✅ Registration Success: $($registerResponse.StatusCode)" -ForegroundColor Green
    $registrationResult = $registerResponse.Content | ConvertFrom-Json
    Write-Host "User Created: $($registrationResult.username)" -ForegroundColor Gray
    $testUsername = $registrationResult.username
} catch {
    Write-Host "❌ Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
    # Use fallback username for login test
    $testUsername = "admin"
}

# Test 3: User Login & JWT Generation
Write-Host "`n🔑 TEST 3: User Login & JWT Token Generation" -ForegroundColor Yellow
$loginData = @{
    username = $testUsername
    password = if ($testUsername -eq "admin") { "admin123" } else { "SecurePass123!" }
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "$authUrl/login" -Method POST -Body $loginData -ContentType "application/json" -UseBasicParsing
    Write-Host "✅ Login Success: $($loginResponse.StatusCode)" -ForegroundColor Green
    
    $loginResult = $loginResponse.Content | ConvertFrom-Json
    $jwtToken = $loginResult.token
    Write-Host "JWT Token Generated: $($jwtToken.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host "Username: $($loginResult.username)" -ForegroundColor Gray
    Write-Host "Roles: $($loginResult.roles)" -ForegroundColor Gray
    Write-Host "Expires in: $($loginResult.expiresIn) seconds" -ForegroundColor Gray
} catch {
    Write-Host "❌ Login Failed: $($_.Exception.Message)" -ForegroundColor Red
    $jwtToken = $null
}

# Test 4: JWT Token Validation
Write-Host "`n🔍 TEST 4: JWT Token Validation" -ForegroundColor Yellow
if ($jwtToken) {
    $validateData = @{
        token = $jwtToken
    } | ConvertTo-Json
    
    try {
        $validateResponse = Invoke-WebRequest -Uri "$authUrl/validate" -Method POST -Body $validateData -ContentType "application/json" -UseBasicParsing
        Write-Host "✅ Token Validation Success: $($validateResponse.StatusCode)" -ForegroundColor Green
        
        $validateResult = $validateResponse.Content | ConvertFrom-Json
        Write-Host "Token Valid: $($validateResult.valid)" -ForegroundColor Gray
        Write-Host "Username from Token: $($validateResult.username)" -ForegroundColor Gray
    } catch {
        Write-Host "❌ Token Validation Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️ Skipping token validation - no JWT token available" -ForegroundColor Yellow
}

# Test 5: Protected Endpoint Access (Expected to fail - no microservice)
Write-Host "`n🔒 TEST 5: Protected Endpoint Access" -ForegroundColor Yellow
if ($jwtToken) {
    $headers = @{
        "Authorization" = "Bearer $jwtToken"
        "Content-Type" = "application/json"
    }
    
    try {
        $protectedResponse = Invoke-WebRequest -Uri "$baseUrl/api/test" -Method GET -Headers $headers -UseBasicParsing
        Write-Host "✅ Protected Access Success: $($protectedResponse.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($protectedResponse.Content)" -ForegroundColor Gray
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "⚠️ Protected endpoint returned 404 - Expected (no microservice implemented)" -ForegroundColor Yellow
        } elseif ($_.Exception.Response.StatusCode -eq 401) {
            Write-Host "❌ JWT Authentication failed at Gateway" -ForegroundColor Red
        } else {
            Write-Host "❌ Protected Access Failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "⚠️ Skipping protected access - no JWT token available" -ForegroundColor Yellow
}

# Test 6: Invalid JWT Token Test
Write-Host "`n🚫 TEST 6: Invalid JWT Token Test" -ForegroundColor Yellow
$invalidHeaders = @{
    "Authorization" = "Bearer invalid.jwt.token"
    "Content-Type" = "application/json"
}

try {
    $invalidResponse = Invoke-WebRequest -Uri "$baseUrl/api/test" -Method GET -Headers $invalidHeaders -UseBasicParsing
    Write-Host "❌ Invalid token was accepted - SECURITY ISSUE!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Invalid token correctly rejected with 401 Unauthorized" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Unexpected response for invalid token: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

# Test 7: Rate Limiting Test (Optional)
Write-Host "`n⚡ TEST 7: Rate Limiting Test" -ForegroundColor Yellow
Write-Host "Testing rate limiting by making multiple requests..." -ForegroundColor Gray

$rateLimitCount = 0
$rateLimitBlocked = $false

for ($i = 1; $i -le 12; $i++) {
    try {
        $quickResponse = Invoke-WebRequest -Uri "$authUrl/validate" -Method POST -Body '{"token":"test"}' -ContentType "application/json" -UseBasicParsing -TimeoutSec 2
        $rateLimitCount++
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "✅ Rate limiting activated after $rateLimitCount requests" -ForegroundColor Green
            $rateLimitBlocked = $true
            break
        }
    }
    Start-Sleep -Milliseconds 100
}

if (-not $rateLimitBlocked) {
    Write-Host "⚠️ Rate limiting not triggered after $rateLimitCount requests" -ForegroundColor Yellow
}

# Test Summary
Write-Host "`n📋 TEST SUMMARY" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host "Gateway Health: $(if ($healthResponse.StatusCode -eq 200) { '✅ OK' } else { '❌ FAIL' })" -ForegroundColor $(if ($healthResponse.StatusCode -eq 200) { 'Green' } else { 'Red' })
Write-Host "User Registration: $(if ($registerResponse.StatusCode -eq 200) { '✅ OK' } else { '⚠️ SKIP' })" -ForegroundColor $(if ($registerResponse.StatusCode -eq 200) { 'Green' } else { 'Yellow' })
Write-Host "JWT Login: $(if ($jwtToken) { '✅ OK' } else { '❌ FAIL' })" -ForegroundColor $(if ($jwtToken) { 'Green' } else { 'Red' })
Write-Host "JWT Validation: $(if ($validateResult.valid) { '✅ OK' } else { '❌ FAIL' })" -ForegroundColor $(if ($validateResult.valid) { 'Green' } else { 'Red' })
Write-Host "Security (401 for invalid): $(if ($_.Exception.Response.StatusCode -eq 401) { '✅ OK' } else { '❌ FAIL' })" -ForegroundColor Green
Write-Host "Rate Limiting: $(if ($rateLimitBlocked) { '✅ OK' } else { '⚠️ NOT TRIGGERED' })" -ForegroundColor $(if ($rateLimitBlocked) { 'Green' } else { 'Yellow' })

Write-Host "`n🎯 AUTHENTICATION FLOW TEST COMPLETED" -ForegroundColor Cyan
