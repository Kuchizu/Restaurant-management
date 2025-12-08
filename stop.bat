@echo off
echo Stopping all services...

taskkill /F /FI "WINDOWTITLE eq Eureka*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Config*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Menu*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Kitchen*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Order*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Inventory*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Billing*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Gateway*" 2>nul

echo Stopping databases...
docker-compose down

echo Done.
