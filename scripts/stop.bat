@echo off
cd /d %~dp0..

echo Stopping all restaurant services...
echo.

echo [1/3] Stopping microservices...
taskkill /F /FI "WINDOWTITLE eq Gateway*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Billing*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Inventory*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Order*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Kitchen*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Menu*" 2>nul
timeout /t 3 /nobreak >nul

echo [2/3] Stopping infrastructure...
taskkill /F /FI "WINDOWTITLE eq Config*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Eureka*" 2>nul
timeout /t 2 /nobreak >nul

echo [3/3] Stopping databases...
docker-compose down

echo.
echo All services stopped.
echo.
echo To free more RAM, stop Gradle daemons:
echo   gradlew --stop
echo.
