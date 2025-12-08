@echo off
set API=http://localhost:49999

echo === Menu Service ===
curl -s %API%/api/menu/dishes
echo.

echo === Order Service ===
curl -s %API%/api/tables
echo.

echo Creating order...
curl -s -X POST %API%/api/orders -H "Content-Type: application/json" -d "{\"tableId\":1,\"waiterId\":1}"
echo.

echo Adding item...
curl -s -X POST %API%/api/orders/1/items -H "Content-Type: application/json" -d "{\"dishId\":1,\"quantity\":2}"
echo.

echo Sending to kitchen...
curl -s -X POST %API%/api/orders/1/send-to-kitchen
echo.

echo === Kitchen Queue ===
curl -s %API%/api/kitchen/queue
echo.

echo === Billing Service (Feign + Circuit Breaker) ===
curl -s -X POST %API%/api/bills/generate/1
echo.

curl -s %API%/api/bills/order/1
echo.

echo === Inventory ===
curl -s %API%/api/inventory
echo.

echo.
echo Tests complete.
