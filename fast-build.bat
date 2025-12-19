@echo off
REM 🚀 ULTRA-FAST Docker Build & Run Script (Windows)
REM ⚡ Uses BuildKit, parallel builds, and caching

echo 🚀 Starting ULTRA-FAST build...
echo ================================

REM Enable BuildKit
set DOCKER_BUILDKIT=1
set COMPOSE_DOCKER_CLI_BUILD=1

echo 🧹 Cleaning old containers...
docker-compose down --remove-orphans

echo 🔨 Building and starting services (parallel mode)...
docker-compose up --build -d

echo.
echo ✅ Build complete!
echo ================================
echo.

timeout /t 5 /nobreak >nul

docker-compose ps

echo.
echo 🌐 Services available at:
echo   - API Gateway:     http://localhost:49999
echo   - Eureka:          http://localhost:8761
echo   - Config Server:   http://localhost:8888
echo   - Order Service:   http://localhost:8081
echo   - Kitchen Service: http://localhost:8082
echo   - Menu Service:    http://localhost:8083 (WebFlux + JPA)
echo   - Inventory:       http://localhost:8084
echo   - Billing:         http://localhost:8085
echo.
echo 📝 View logs: docker-compose logs -f [service-name]
echo 🛑 Stop all:  docker-compose down
echo.
echo ⏳ Services starting... Full startup in ~2-3 minutes
