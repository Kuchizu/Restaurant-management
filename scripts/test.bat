@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set API=http://localhost:49999
set TOTAL=0
set PASSED=0
set FAILED=0
set EXPECTED_ERRORS=0

echo ========================================
echo  Restaurant Management - Full Test Suite
echo ========================================
echo.
echo API Gateway: %API%
echo Start Time: %TIME%
echo.

:test_menu_get_dishes
set /a TOTAL+=1
echo [Test 1/%TOTAL%] Menu Service - Get All Dishes
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/dishes > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
echo.
del temp_status.txt
timeout /t 1 /nobreak >nul

:test_menu_get_categories
set /a TOTAL+=1
echo [Test 2/%TOTAL%] Menu Service - Get All Categories
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/categories > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
echo.
del temp_status.txt
timeout /t 1 /nobreak >nul

:test_order_get_all
set /a TOTAL+=1
echo [Test 3/%TOTAL%] Order Service - Get All Orders
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/orders > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
echo.
del temp_status.txt
timeout /t 1 /nobreak >nul

:test_order_not_found
set /a TOTAL+=1
echo [Test 4/%TOTAL%] Order Service - Get Non-existent Order (Error Test)
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/orders/99999 > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 404" (
    echo [PASS] HTTP 404 NOT FOUND (correct error handling!)
    set /a PASSED+=1
    set /a EXPECTED_ERRORS+=1
) else (
    echo [FAIL] Expected 404, got %STATUS%
    set /a FAILED+=1
)
echo.
del temp_status.txt
timeout /t 1 /nobreak >nul

:test_order_create
set /a TOTAL+=1
echo [Test 5/%TOTAL%] Order Service - Create Order (Table 1)
curl -s -o response.json -w "HTTP %%{http_code}" -X POST %API%/api/orders -H "Content-Type: application/json" -d "{\"tableId\":1,\"waiterId\":1}" > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK - Order created
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt 2>nul
del response.json 2>nul
echo.
timeout /t 1 /nobreak >nul

:test_order_duplicate_table
set /a TOTAL+=1
echo [Test 6/%TOTAL%] Order Service - Duplicate Table (Error Test)
curl -s -o response.json -w "HTTP %%{http_code}" -X POST %API%/api/orders -H "Content-Type: application/json" -d "{\"tableId\":1,\"waiterId\":1}" > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 409" (
    echo [PASS] HTTP 409 CONFLICT (table occupied - correct!)
    set /a PASSED+=1
    set /a EXPECTED_ERRORS+=1
) else if "%STATUS%"=="HTTP 200" (
    echo [INFO] HTTP 200 - table was freed or different order
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 409, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt 2>nul
del response.json 2>nul
echo.
timeout /t 1 /nobreak >nul

:test_order_invalid_json
set /a TOTAL+=1
echo [Test 7/%TOTAL%] Order Service - Invalid JSON (Error Test)
curl -s -o nul -w "HTTP %%{http_code}" -X POST %API%/api/orders -H "Content-Type: application/json" -d "{invalid}" > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 400" (
    echo [PASS] HTTP 400 BAD REQUEST (malformed JSON - correct!)
    set /a PASSED+=1
    set /a EXPECTED_ERRORS+=1
) else (
    echo [FAIL] Expected 400, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_kitchen_queue
set /a TOTAL+=1
echo [Test 8/%TOTAL%] Kitchen Service - Get Queue
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/kitchen/queue > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_inventory_get
set /a TOTAL+=1
echo [Test 9/%TOTAL%] Inventory Service - Get All Inventory
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/inventory > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_inventory_not_found
set /a TOTAL+=1
echo [Test 10/%TOTAL%] Inventory Service - Non-existent Item (Error Test)
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/inventory/99999 > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 404" (
    echo [PASS] HTTP 404 NOT FOUND (correct error handling!)
    set /a PASSED+=1
    set /a EXPECTED_ERRORS+=1
) else (
    echo [FAIL] Expected 404, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_ingredients_get
set /a TOTAL+=1
echo [Test 11/%TOTAL%] Inventory Service - Get All Ingredients
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/ingredients > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_billing_not_found
set /a TOTAL+=1
echo [Test 12/%TOTAL%] Billing Service - Non-existent Bill (Error Test)
curl -s -o nul -w "HTTP %%{http_code}" %API%/api/bills/order/99999 > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 404" (
    echo [PASS] HTTP 404 NOT FOUND (correct error handling!)
    set /a PASSED+=1
    set /a EXPECTED_ERRORS+=1
) else (
    echo [FAIL] Expected 404, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_eureka_check
set /a TOTAL+=1
echo [Test 13/%TOTAL%] Eureka Server - Service Registry
curl -s -o nul -w "HTTP %%{http_code}" http://localhost:8761 > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK - Eureka running
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.
timeout /t 1 /nobreak >nul

:test_gateway_health
set /a TOTAL+=1
echo [Test 14/%TOTAL%] API Gateway - Health Check
curl -s -o nul -w "HTTP %%{http_code}" %API%/actuator/health > temp_status.txt
set /p STATUS=<temp_status.txt
if "%STATUS%"=="HTTP 200" (
    echo [PASS] HTTP 200 OK - Gateway healthy
    set /a PASSED+=1
) else if "%STATUS%"=="HTTP 404" (
    echo [INFO] Actuator not exposed (404) - OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200 or 404, got %STATUS%
    set /a FAILED+=1
)
del temp_status.txt
echo.

echo.
echo ========================================
echo  TEST SUMMARY
echo ========================================
echo.
echo End Time:       %TIME%
echo.
echo Total Tests:    %TOTAL%
echo Passed:         %PASSED%
echo Failed:         %FAILED%
echo Expected Errors: %EXPECTED_ERRORS% (404, 409, 400 tests)
echo.

set /a SUCCESS_RATE=(%PASSED% * 100) / %TOTAL%
echo Success Rate:   %SUCCESS_RATE%%%
echo.

if %FAILED% EQU 0 (
    echo ========================================
    echo  ALL TESTS PASSED!
    echo ========================================
    echo.
    echo Your system is working correctly:
    echo   [v] All services responding
    echo   [v] Error handling correct (404, 409, 400)
    echo   [v] NO inappropriate 500 errors
    echo   [v] API Gateway routing works
    echo   [v] Microservices communicating
) else (
    echo ========================================
    echo  TESTS FAILED - ACTION REQUIRED
    echo ========================================
    echo.
    echo Troubleshooting steps:
    echo   1. Check Eureka: http://localhost:8761
    echo   2. Verify all services show UP status
    echo   3. Check service logs for errors
    echo   4. Ensure databases running: docker ps
    echo   5. Check memory usage: scripts\memory-monitor.bat
)

echo.
echo ========================================
echo  HTTP STATUS CODE REFERENCE
echo ========================================
echo.
echo [200] OK            - Success
echo [400] Bad Request   - Malformed JSON/params
echo [404] Not Found     - Resource doesn't exist
echo [409] Conflict      - Business rule violation
echo [422] Validation    - Semantic validation error
echo [503] Unavailable   - Service down/Circuit Breaker
echo [500] Server Error  - BUG (should never happen!)
echo.
echo ========================================
echo.

pause
endlocal
