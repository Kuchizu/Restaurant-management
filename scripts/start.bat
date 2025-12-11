@echo off
cd /d %~dp0..

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
start "Eureka" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx384m -Xms128m -XX:MaxMetaspaceSize=192m" && gradlew.bat :eureka-server:bootRun"
timeout /t 15 /nobreak >nul

start "Config" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx384m -Xms128m -XX:MaxMetaspaceSize=192m" && gradlew.bat :config-server:bootRun"
timeout /t 12 /nobreak >nul

echo [3/3] Starting services sequentially (optimized for 16GB RAM)...
start "Menu" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx448m -Xms128m -XX:MaxMetaspaceSize=224m" && gradlew.bat :menu-service:bootRun"
timeout /t 8 /nobreak >nul

start "Kitchen" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx448m -Xms128m -XX:MaxMetaspaceSize=224m" && gradlew.bat :kitchen-service:bootRun"
timeout /t 8 /nobreak >nul

start "Order" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m" && gradlew.bat :order-service:bootRun"
timeout /t 8 /nobreak >nul

start "Inventory" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m" && gradlew.bat :inventory-service:bootRun"
timeout /t 8 /nobreak >nul

start "Billing" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx448m -Xms128m -XX:MaxMetaspaceSize=224m" && gradlew.bat :billing-service:bootRun"
timeout /t 10 /nobreak >nul

start "Gateway" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx448m -Xms128m -XX:MaxMetaspaceSize=224m" && gradlew.bat :api-gateway:bootRun"

echo.
echo Services starting with OPTIMIZED memory for 16GB laptop:
echo   Infrastructure: 384MB each (Eureka, Config)
echo   Business Logic: 448-512MB each (Order, Inventory higher; others 448MB)
echo   Total RAM usage: ~3.5GB services + 1GB Gradle + 1GB DBs = ~5.5GB
echo.
echo Wait 2-3 minutes for all services to initialize, then check:
echo   Eureka Dashboard: http://localhost:8761
echo   API Gateway:      http://localhost:49999
echo.
echo Services: Menu (8083), Kitchen (8082), Order (8081), Inventory (8084), Billing (8085)
echo.
