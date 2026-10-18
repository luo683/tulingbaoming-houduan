# Switch to H2 Database Script
# Run this to avoid MySQL installation

Write-Host "Switching to H2 in-memory database..." -ForegroundColor Green
Write-Host ""

$pomPath = "后端\tuling\pom.xml"
$ymlPath = "后端\tuling\src\main\resources\application.yml"

# Backup files
Write-Host "[1/3] Creating backups..." -ForegroundColor Cyan
Copy-Item $pomPath "$pomPath.backup"
Copy-Item $ymlPath "$ymlPath.backup"
Write-Host "      Done" -ForegroundColor Green

# Modify pom.xml
Write-Host "[2/3] Updating pom.xml..." -ForegroundColor Cyan
$pomContent = Get-Content $pomPath -Raw
$pomContent = $pomContent -replace '(?s)(<dependency>\s*<groupId>com\.mysql</groupId>.*?</dependency>)', '<!-- MySQL commented out
$1
-->'
$pomContent = $pomContent -replace '(</dependencies>)', @"
        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
    `$1
"@
Set-Content $pomPath $pomContent -Encoding UTF8
Write-Host "      Done" -ForegroundColor Green

# Modify application.yml
Write-Host "[3/3] Updating application.yml..." -ForegroundColor Cyan
$ymlContent = @"
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

  redis:
    host: localhost
    port: 6379
    password:
    timeout: 2000ms

ai:
  model:
    base-url: https://api.siliconflow.cn/v1
    api-key: ${SILICONFLOW_API_KEY:}
    model-name: Qwen/Qwen2.5-7B-Instruct
    temperature: 0.7
    max-tokens: 2048
"@
Set-Content $ymlPath $ymlContent -Encoding UTF8
Write-Host "      Done" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Configuration Updated Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. cd 'E:\图灵 报名功能\后端\tuling'" -ForegroundColor White
Write-Host "2. mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backups created:" -ForegroundColor Yellow
Write-Host "  - pom.xml.backup"
Write-Host "  - application.yml.backup"
Write-Host ""
Read-Host "Press Enter to close"
