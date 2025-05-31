# =================================================================
# ENVIRONMENT SETUP SCRIPT
# =================================================================
# Run this script to set up environment variables for local development

# Usage (PowerShell):
# . .\setup-env.ps1

# =================================================================
# JWT SECURITY CONFIGURATION
# =================================================================
Write-Host "🔐 Setting up JWT Security Configuration..."

# Generate a secure JWT secret (32 bytes = 256 bits)
$JWT_SECRET = -join ((1..64) | ForEach {Get-Random -Maximum 16 | ForEach {'{0:X}' -f $_}})
[Environment]::SetEnvironmentVariable("JWT_SECRET", $JWT_SECRET, "User")

[Environment]::SetEnvironmentVariable("JWT_ISSUER", "spring-microservices", "User")
[Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MS", "86400000", "User")

Write-Host "✅ JWT_SECRET generated and set"

# =================================================================
# DATABASE CONFIGURATION
# =================================================================
Write-Host "🗄️ Setting up Database Configuration..."

[Environment]::SetEnvironmentVariable("DATABASE_URL", "jdbc:postgresql://localhost:5432/springdb", "User")
[Environment]::SetEnvironmentVariable("DATABASE_USERNAME", "springuser", "User")
[Environment]::SetEnvironmentVariable("DATABASE_PASSWORD", "dev_password_123", "User")
[Environment]::SetEnvironmentVariable("POSTGRES_PASSWORD", "postgres_admin_123", "User")

Write-Host "✅ Database environment variables set"

# =================================================================
# SSL CONFIGURATION
# =================================================================
Write-Host "🔒 Setting up SSL Configuration..."

[Environment]::SetEnvironmentVariable("SSL_KEYSTORE_PASSWORD", "changeit", "User")

Write-Host "✅ SSL environment variables set"

# =================================================================
# REDIS CONFIGURATION
# =================================================================
Write-Host "📦 Setting up Redis Configuration..."

[Environment]::SetEnvironmentVariable("REDIS_HOST", "localhost", "User")
[Environment]::SetEnvironmentVariable("REDIS_PORT", "6379", "User")
[Environment]::SetEnvironmentVariable("REDIS_PASSWORD", "", "User")

Write-Host "✅ Redis environment variables set"

# =================================================================
# SPRING PROFILES
# =================================================================
Write-Host "🚀 Setting up Spring Profiles..."

[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "dev", "User")

Write-Host "✅ Spring profiles set"

# =================================================================
# SUMMARY
# =================================================================
Write-Host ""
Write-Host "🎉 Environment setup completed!"
Write-Host ""
Write-Host "Environment Variables Set:"
Write-Host "- JWT_SECRET: [GENERATED]"
Write-Host "- JWT_ISSUER: spring-microservices"
Write-Host "- DATABASE_URL: jdbc:postgresql://localhost:5432/springdb"
Write-Host "- DATABASE_USERNAME: springuser"
Write-Host "- SPRING_PROFILES_ACTIVE: dev"
Write-Host ""
Write-Host "⚠️  IMPORTANT:"
Write-Host "1. Restart your terminal/IDE to use new environment variables"
Write-Host "2. For production, use STRONG passwords and secrets"
Write-Host "3. Never commit real passwords to version control"
Write-Host ""
Write-Host "To verify setup:"
Write-Host 'Get-ChildItem env: | Where-Object Name -like "*JWT*"'
