@echo off
REM Circuit Breaker Test Script for Windows
REM Tests all circuit breakers in the restaurant management system

echo =========================================
echo Circuit Breaker Testing Script
echo =========================================
echo.

REM Get service ports from Eureka (requires jq - install from chocolatey: choco install jq)
echo Detecting service ports from Eureka...

for /f %%i in ('curl -s http://localhost:8761/eureka/apps/ORDER-SERVICE ^| findstr /r "<port>[0-9]*</port>" ^| findstr /o /r "[0-9][0-9]*" ^| findstr /b "7:"') do set ORDER_PORT=%%i
for /f %%i in ('curl -s http://localhost:8761/eureka/apps/BILLING-SERVICE ^| findstr /r "<port>[0-9]*</port>" ^| findstr /o /r "[0-9][0-9]*" ^| findstr /b "7:"') do set BILLING_PORT=%%i

echo Service Ports:
echo   Order Service: %ORDER_PORT%
echo   Billing Service: %BILLING_PORT%
echo.

echo =========================================
echo Test 1: Circuit Breaker Health Status
echo =========================================

echo Order Service Circuit Breakers:
curl -s http://localhost:%ORDER_PORT%/actuator/health

echo.
echo Billing Service Circuit Breakers:
curl -s http://localhost:%BILLING_PORT%/actuator/health

echo.
echo =========================================
echo Test 2: Manual Circuit Breaker Test
echo =========================================
echo.
echo INSTRUCTIONS:
echo 1. Stop kitchen-service: docker-compose stop kitchen-service
echo 2. Make 10-15 requests to order-service to trigger circuit breaker
echo 3. Check health endpoint to see circuit breaker state OPEN
echo 4. Restart kitchen-service: docker-compose start kitchen-service
echo 5. Wait 10 seconds for circuit breaker to transition to HALF_OPEN
echo 6. Make successful requests to close circuit breaker
echo 7. Check health endpoint to see circuit breaker state CLOSED
echo.

echo =========================================
echo Test 3: Check Circuit Breaker Metrics
echo =========================================

echo Order Service Metrics:
curl -s http://localhost:%ORDER_PORT%/actuator/metrics/resilience4j.circuitbreaker.calls

echo.
echo Billing Service Metrics:
curl -s http://localhost:%BILLING_PORT%/actuator/metrics/resilience4j.circuitbreaker.calls

echo.
echo =========================================
echo Test Complete!
echo =========================================
pause
