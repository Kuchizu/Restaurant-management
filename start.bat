@echo off
echo Stopping any running services...
taskkill /F /FI "WINDOWTITLE eq Eureka*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Config*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Menu*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Kitchen*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Order*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Inventory*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Billing*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Gateway*" 2>nul
timeout /t 3 /nobreak >nul

if not exist logs mkdir logs

echo [1/3] Starting databases...
docker-compose up -d
timeout /t 10 /nobreak >nul

echo [2/3] Starting infrastructure...
start "Eureka" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :eureka-server:bootRun"
timeout /t 15 /nobreak >nul

start "Config" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :config-server:bootRun"
timeout /t 10 /nobreak >nul

echo [3/3] Starting services...
start "Menu" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :menu-service:bootRun"
start "Kitchen" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :kitchen-service:bootRun"
start "Order" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :order-service:bootRun"
start "Inventory" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :inventory-service:bootRun"
start "Billing" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :billing-service:bootRun"
timeout /t 20 /nobreak >nul

start "Gateway" cmd /k "cd /d %~dp0 && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && gradlew.bat :api-gateway:bootRun"

echo.
echo Services starting with limited memory (256MB each)...
echo Wait 1-2 minutes, then check:
echo   Eureka: http://localhost:8761
echo   Gateway: http://localhost:49999
echo.
