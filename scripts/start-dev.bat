@echo off
cd /d %~dp0..

echo ========================================
echo  DEVELOPMENT MODE - Essential Services
echo ========================================
echo.
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

echo [3/3] Starting CORE services only (Menu + Order)...
start "Menu" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m" && gradlew.bat :menu-service:bootRun"
timeout /t 10 /nobreak >nul

start "Order" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m" && gradlew.bat :order-service:bootRun"
timeout /t 15 /nobreak >nul

start "Gateway" cmd /k "cd /d %~dp0.. && set "JAVA_HOME=%ProgramFiles%\Eclipse Adoptium\jdk-21.0.9.10-hotspot" && set "JAVA_OPTS=-Xmx448m -Xms128m -XX:MaxMetaspaceSize=224m" && gradlew.bat :api-gateway:bootRun"

echo.
echo ========================================
echo  DEVELOPMENT MODE STARTED
echo ========================================
echo.
echo Running services (MINIMAL RAM ~2.5GB):
echo   [x] Eureka Server (8761) - 384MB
echo   [x] Config Server (8888) - 384MB
echo   [x] Menu Service (8083)  - 512MB
echo   [x] Order Service (8081) - 512MB
echo   [x] API Gateway (49999)  - 448MB
echo.
echo NOT running (start manually if needed):
echo   [ ] Kitchen Service (8082)
echo   [ ] Inventory Service (8084)
echo   [ ] Billing Service (8085)
echo.
echo To start additional services manually:
echo   gradlew.bat :kitchen-service:bootRun
echo   gradlew.bat :inventory-service:bootRun
echo   gradlew.bat :billing-service:bootRun
echo.
echo Wait 1-2 minutes, then check:
echo   Eureka: http://localhost:8761
echo   Gateway: http://localhost:49999
echo.
