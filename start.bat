@echo off
if not exist logs mkdir logs

echo [1/3] Starting databases...
docker-compose up -d
timeout /t 10 /nobreak >nul

echo [2/3] Starting infrastructure...
start "Eureka" cmd /c "gradlew.bat :eureka-server:bootRun"
timeout /t 15 /nobreak >nul

start "Config" cmd /c "gradlew.bat :config-server:bootRun"
timeout /t 10 /nobreak >nul

echo [3/3] Starting services...
start "Menu" cmd /c "gradlew.bat :menu-service:bootRun"
start "Kitchen" cmd /c "gradlew.bat :kitchen-service:bootRun"
start "Order" cmd /c "gradlew.bat :order-service:bootRun"
start "Inventory" cmd /c "gradlew.bat :inventory-service:bootRun"
start "Billing" cmd /c "gradlew.bat :billing-service:bootRun"
timeout /t 20 /nobreak >nul

start "Gateway" cmd /c "gradlew.bat :api-gateway:bootRun"

echo.
echo Services starting...
echo Wait 1-2 minutes, then check:
echo   Eureka: http://localhost:8761
echo   Gateway: http://localhost:49999
echo.
