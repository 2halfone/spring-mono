# 🔐 SECURITY ISOLATION TEST SCRIPT
# Test per verificare che solo il gateway sia accessibile esternamente

Write-Host "🔐 TESTING SECURITY ISOLATION" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""

# Funzione per testare connettività
function Test-ServiceAccess {
    param(
        [string]$ServiceName,
        [int]$Port,
        [string]$ExpectedResult
    )
    
    Write-Host "Testing $ServiceName on port $Port..." -ForegroundColor Yellow
    
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue
        
        if ($connection.TcpTestSucceeded) {
            if ($ExpectedResult -eq "ACCESSIBLE") {
                Write-Host "✅ $ServiceName is accessible (EXPECTED)" -ForegroundColor Green
                return $true
            } else {
                Write-Host "❌ $ServiceName is accessible (SECURITY RISK!)" -ForegroundColor Red
                return $false
            }
        } else {
            if ($ExpectedResult -eq "BLOCKED") {
                Write-Host "✅ $ServiceName is blocked (SECURE)" -ForegroundColor Green
                return $true
            } else {
                Write-Host "❌ $ServiceName is not accessible (SERVICE DOWN?)" -ForegroundColor Red
                return $false
            }
        }
    }
    catch {
        Write-Host "❌ Error testing $ServiceName : $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Funzione per testare endpoint HTTP
function Test-HttpEndpoint {
    param(
        [string]$Url,
        [string]$ServiceName,
        [string]$ExpectedResult
    )
    
    Write-Host "Testing HTTP endpoint: $Url" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -ErrorAction SilentlyContinue
        
        if ($response.StatusCode -eq 200) {
            if ($ExpectedResult -eq "ACCESSIBLE") {
                Write-Host "✅ $ServiceName HTTP endpoint accessible (EXPECTED)" -ForegroundColor Green
                return $true
            } else {
                Write-Host "❌ $ServiceName HTTP endpoint accessible (SECURITY RISK!)" -ForegroundColor Red
                return $false
            }
        }
    }
    catch {
        if ($ExpectedResult -eq "BLOCKED") {
            Write-Host "✅ $ServiceName HTTP endpoint blocked (SECURE)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ $ServiceName HTTP endpoint not accessible: $($_.Exception.Message)" -ForegroundColor Red
            return $false
        }
    }
    
    return $false
}

Write-Host "🧪 PHASE 1: CURRENT CONFIGURATION TEST" -ForegroundColor Magenta
Write-Host "Testing current staging configuration..."
Write-Host ""

# Test current configuration (dovrebbe mostrare vulnerabilità)
$currentResults = @{}
$currentResults["Gateway"] = Test-ServiceAccess -ServiceName "Gateway" -Port 9080 -ExpectedResult "ACCESSIBLE"
$currentResults["Auth-Service"] = Test-ServiceAccess -ServiceName "Auth-Service" -Port 9081 -ExpectedResult "BLOCKED"
$currentResults["PostgreSQL"] = Test-ServiceAccess -ServiceName "PostgreSQL" -Port 15432 -ExpectedResult "BLOCKED"
$currentResults["Redis"] = Test-ServiceAccess -ServiceName "Redis" -Port 16379 -ExpectedResult "BLOCKED"

Write-Host ""
Write-Host "🧪 PHASE 2: HTTP ENDPOINT SECURITY TEST" -ForegroundColor Magenta
Write-Host "Testing HTTP endpoints accessibility..."
Write-Host ""

# Test HTTP endpoints
$httpResults = @{}
$httpResults["Gateway-Health"] = Test-HttpEndpoint -Url "http://localhost:9080/actuator/health" -ServiceName "Gateway" -ExpectedResult "ACCESSIBLE"
$httpResults["Auth-Direct"] = Test-HttpEndpoint -Url "http://localhost:9081/actuator/health" -ServiceName "Auth-Service Direct" -ExpectedResult "BLOCKED"

Write-Host ""
Write-Host "🧪 PHASE 3: SECURE CONFIGURATION VALIDATION" -ForegroundColor Magenta
Write-Host "Checking if secure configuration exists..."
Write-Host ""

# Controlla se la configurazione sicura esiste
$secureComposeExists = Test-Path "docker-compose.secure.yml"
$secureGatewayConfigExists = Test-Path "gateway\initial\src\main\resources\application-secure.properties"

Write-Host "Secure docker-compose.yml: $(if($secureComposeExists){"✅ EXISTS"}else{"❌ MISSING"})" -ForegroundColor $(if($secureComposeExists){"Green"}else{"Red"})
Write-Host "Secure gateway config: $(if($secureGatewayConfigExists){"✅ EXISTS"}else{"❌ MISSING"})" -ForegroundColor $(if($secureGatewayConfigExists){"Green"}else{"Red"})

Write-Host ""
Write-Host "📊 SECURITY ASSESSMENT RESULTS" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

$passedTests = 0
$totalTests = $currentResults.Count + $httpResults.Count

foreach ($test in $currentResults.GetEnumerator()) {
    $status = if ($test.Value) { "PASS" } else { "FAIL" }
    $color = if ($test.Value) { "Green" } else { "Red" }
    Write-Host "$($test.Key): $status" -ForegroundColor $color
    if ($test.Value) { $passedTests++ }
}

foreach ($test in $httpResults.GetEnumerator()) {
    $status = if ($test.Value) { "PASS" } else { "FAIL" }
    $color = if ($test.Value) { "Green" } else { "Red" }
    Write-Host "$($test.Key): $status" -ForegroundColor $color
    if ($test.Value) { $passedTests++ }
}

Write-Host ""
$securityScore = [math]::Round(($passedTests / $totalTests) * 100, 1)
$scoreColor = if ($securityScore -ge 80) { "Green" } elseif ($securityScore -ge 60) { "Yellow" } else { "Red" }

Write-Host "🎯 SECURITY SCORE: $securityScore% ($passedTests/$totalTests)" -ForegroundColor $scoreColor
Write-Host ""

if ($securityScore -lt 100) {
    Write-Host "⚠️  SECURITY RECOMMENDATIONS:" -ForegroundColor Red
    Write-Host "1. Deploy secure Docker configuration (docker-compose.secure.yml)" -ForegroundColor Yellow
    Write-Host "2. Remove external port mappings from internal services" -ForegroundColor Yellow
    Write-Host "3. Use internal Docker networking for service communication" -ForegroundColor Yellow
    Write-Host "4. Only expose gateway ports externally" -ForegroundColor Yellow
} else {
    Write-Host "🎉 SECURITY CONFIGURATION IS OPTIMAL!" -ForegroundColor Green
}

Write-Host ""
Write-Host "🚀 NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Stop current services: docker-compose -f docker-compose.staging.yml down" -ForegroundColor White
Write-Host "2. Deploy secure config: docker-compose -f docker-compose.secure.yml up -d" -ForegroundColor White
Write-Host "3. Re-run this test to verify security improvements" -ForegroundColor White

Write-Host ""
Write-Host "📁 Generated report: SECURITY_TEST_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt" -ForegroundColor Gray

# Salva risultati su file
$reportContent = @"
SECURITY ISOLATION TEST REPORT
Generated: $(Get-Date)
==============================

CURRENT CONFIGURATION RESULTS:
$(foreach($r in $currentResults.GetEnumerator()){"$($r.Key): $(if($r.Value){"PASS"}else{"FAIL"})"})

HTTP ENDPOINT RESULTS:
$(foreach($r in $httpResults.GetEnumerator()){"$($r.Key): $(if($r.Value){"PASS"}else{"FAIL"})"})

SECURITY SCORE: $securityScore% ($passedTests/$totalTests)

RECOMMENDATIONS:
$(if($securityScore -lt 100){
"- Deploy secure Docker configuration
- Remove external port mappings from internal services  
- Use internal Docker networking for service communication
- Only expose gateway ports externally"
}else{
"✅ Security configuration is optimal"
})
"@

$reportContent | Out-File -FilePath "SECURITY_TEST_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt" -Encoding UTF8

Write-Host "Test completed! Check the generated report for details." -ForegroundColor Green
