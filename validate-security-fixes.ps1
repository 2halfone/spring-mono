#!/usr/bin/env powershell

# 🔐 SECURITY FIXES VALIDATION SCRIPT
# Validates the immediate pre-production security fixes implemented

Write-Host "🔐 VALIDATING SECURITY FIXES..." -ForegroundColor Cyan
Write-Host "=" * 50

# Test 1: Check Gateway Header Size Configuration
Write-Host "`n📋 TEST 1: Validating Gateway Header Size Fix..." -ForegroundColor Yellow
$gatewayConfig = "gateway\initial\src\main\resources\application.properties"
if (Test-Path $gatewayConfig) {
    $headerConfig = Select-String -Path $gatewayConfig -Pattern "max-http-header-size"
    if ($headerConfig) {
        Write-Host "✅ Gateway header size limit configured: $($headerConfig.Line.Trim())" -ForegroundColor Green
    } else {
        Write-Host "❌ Gateway header size limit NOT configured" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Gateway config file not found" -ForegroundColor Red
}

# Test 2: Check Docker Compose Security - Staging
Write-Host "`n🐳 TEST 2: Validating Docker Compose Staging Security..." -ForegroundColor Yellow
$stagingCompose = "docker-compose.staging.yml"
if (Test-Path $stagingCompose) {
    $exposedPorts = Select-String -Path $stagingCompose -Pattern "ports:" -Context 0,2
    $internalNetworks = Select-String -Path $stagingCompose -Pattern "microservices-internal"
    
    if ($internalNetworks) {
        Write-Host "✅ Internal network isolation configured" -ForegroundColor Green
    } else {
        Write-Host "❌ Internal network isolation NOT configured" -ForegroundColor Red
    }
    
    # Check for database port exposure
    $dbPorts = Select-String -Path $stagingCompose -Pattern '"5432:5432"|"15432:5432"|"6379:6379"|"16379:6379"'
    if (-not $dbPorts) {
        Write-Host "✅ Database ports removed from external access" -ForegroundColor Green
    } else {
        Write-Host "❌ Database ports still exposed: $($dbPorts.Line.Trim())" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Staging docker-compose file not found" -ForegroundColor Red
}

# Test 3: Check Docker Compose Security - Development
Write-Host "`n🔧 TEST 3: Validating Docker Compose Development Security..." -ForegroundColor Yellow
$devCompose = "docker-compose.dev.yml"
if (Test-Path $devCompose) {
    $dbPorts = Select-String -Path $devCompose -Pattern '"5432:5432"'
    if (-not $dbPorts) {
        Write-Host "✅ Development database ports secured" -ForegroundColor Green
    } else {
        Write-Host "❌ Development database ports still exposed" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Development docker-compose file not found" -ForegroundColor Red
}

# Test 4: Network Port Scan Validation
Write-Host "`n🌐 TEST 4: Port Scanning for Open Database Ports..." -ForegroundColor Yellow

function Test-Port {
    param($hostname, $port, $timeout = 1000)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $connect = $tcpClient.BeginConnect($hostname, $port, $null, $null)
        $wait = $connect.AsyncWaitHandle.WaitOne($timeout, $false)
        if ($wait) {
            $tcpClient.EndConnect($connect)
            $tcpClient.Close()
            return $true
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

# Test previously vulnerable ports
$vulnerablePorts = @(5432, 6379, 15432, 16379)
$openPorts = @()

foreach ($port in $vulnerablePorts) {
    if (Test-Port "localhost" $port) {
        $openPorts += $port
        Write-Host "❌ Port $port is still open and accessible" -ForegroundColor Red
    } else {
        Write-Host "✅ Port $port is properly secured (not accessible)" -ForegroundColor Green
    }
}

# Test 5: Gateway Accessibility
Write-Host "`n🌐 TEST 5: Gateway Access Validation..." -ForegroundColor Yellow
$gatewayPorts = @(8080, 9080, 9443)
$accessibleGateways = @()

foreach ($port in $gatewayPorts) {
    if (Test-Port "localhost" $port) {
        $accessibleGateways += $port
        Write-Host "✅ Gateway port $port is accessible (expected)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Gateway port $port is not accessible" -ForegroundColor Yellow
    }
}

# Summary Report
Write-Host "`n" + "=" * 50
Write-Host "🔐 SECURITY VALIDATION SUMMARY" -ForegroundColor Cyan
Write-Host "=" * 50

if ($openPorts.Count -eq 0) {
    Write-Host "✅ DATABASE SECURITY: All database ports properly secured" -ForegroundColor Green
} else {
    Write-Host "❌ DATABASE SECURITY: $($openPorts.Count) ports still exposed: $($openPorts -join ', ')" -ForegroundColor Red
}

if ($accessibleGateways.Count -gt 0) {
    Write-Host "✅ GATEWAY ACCESS: Gateway properly accessible on ports: $($accessibleGateways -join ', ')" -ForegroundColor Green
} else {
    Write-Host "⚠️  GATEWAY ACCESS: No gateway ports accessible (services may be down)" -ForegroundColor Yellow
}

# Instructions for next steps
Write-Host "`n📋 NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Run services with: docker-compose -f docker-compose.staging.yml up -d" -ForegroundColor White
Write-Host "2. Run penetration test: .\run-pentest.ps1" -ForegroundColor White
Write-Host "3. Check for remaining vulnerabilities" -ForegroundColor White

Write-Host "`n🔐 Security validation completed!" -ForegroundColor Green
