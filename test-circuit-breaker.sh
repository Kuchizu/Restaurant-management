#!/bin/bash

# Circuit Breaker Test Script
# Tests all circuit breakers in the restaurant management system

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================="
echo "Circuit Breaker Testing Script"
echo "========================================="
echo ""

# Find service ports from Eureka
ORDER_PORT=$(curl -s http://localhost:8761/eureka/apps/ORDER-SERVICE | grep -oP '(?<=<port>)\d+' | head -1)
KITCHEN_PORT=$(curl -s http://localhost:8761/eureka/apps/KITCHEN-SERVICE | grep -oP '(?<=<port>)\d+' | head -1)
MENU_PORT=$(curl -s http://localhost:8761/eureka/apps/MENU-SERVICE | grep -oP '(?<=<port>)\d+' | head -1)
BILLING_PORT=$(curl -s http://localhost:8761/eureka/apps/BILLING-SERVICE | grep -oP '(?<=<port>)\d+' | head -1)

echo "Service Ports:"
echo "  Order Service: $ORDER_PORT"
echo "  Kitchen Service: $KITCHEN_PORT"
echo "  Menu Service: $MENU_PORT"
echo "  Billing Service: $BILLING_PORT"
echo ""

# Test 1: Check circuit breaker health indicators
echo "========================================="
echo "Test 1: Circuit Breaker Health Status"
echo "========================================="

echo "Order Service Circuit Breakers:"
curl -s http://localhost:$ORDER_PORT/actuator/health | jq '.components.circuitBreakers'

echo ""
echo "Billing Service Circuit Breakers:"
curl -s http://localhost:$BILLING_PORT/actuator/health | jq '.components.circuitBreakers'

echo ""
echo "========================================="
echo "Test 2: Trigger Circuit Breaker (kitchenService)"
echo "========================================="

echo "Stopping kitchen-service to trigger circuit breaker..."
docker-compose stop kitchen-service

echo "Waiting 5 seconds..."
sleep 5

echo "Making 15 requests to trigger circuit breaker (threshold: 50% of 10)..."
for i in {1..15}; do
  echo -n "Request $i: "
  RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:$ORDER_PORT/api/orders/1/items \
    -H "Content-Type: application/json" \
    -d '{"dishId": 1, "quantity": 1}')

  if [ "$RESPONSE" == "503" ]; then
    echo -e "${YELLOW}503 Service Unavailable${NC} (expected)"
  else
    echo -e "${RED}$RESPONSE${NC} (unexpected)"
  fi
  sleep 0.5
done

echo ""
echo "Checking circuit breaker state..."
CB_STATE=$(curl -s http://localhost:$ORDER_PORT/actuator/health | jq -r '.components.circuitBreakers.details.kitchenService.details.state')
echo -e "Circuit Breaker State: ${YELLOW}$CB_STATE${NC}"

if [ "$CB_STATE" == "OPEN" ]; then
  echo -e "${GREEN}✓ Circuit breaker is OPEN (correct!)${NC}"
else
  echo -e "${RED}✗ Circuit breaker should be OPEN${NC}"
fi

echo ""
echo "Restarting kitchen-service..."
docker-compose start kitchen-service

echo "Waiting 15 seconds for recovery and transition to HALF_OPEN..."
sleep 15

echo ""
echo "Checking circuit breaker state after wait..."
CB_STATE=$(curl -s http://localhost:$ORDER_PORT/actuator/health | jq -r '.components.circuitBreakers.details.kitchenService.details.state')
echo -e "Circuit Breaker State: ${YELLOW}$CB_STATE${NC}"

echo ""
echo "Making successful requests to close circuit breaker..."
for i in {1..5}; do
  echo -n "Request $i: "
  RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:$ORDER_PORT/api/orders/1/items \
    -H "Content-Type: application/json" \
    -d '{"dishId": 1, "quantity": 1}')

  if [ "$RESPONSE" == "200" ] || [ "$RESPONSE" == "201" ]; then
    echo -e "${GREEN}$RESPONSE Success${NC}"
  else
    echo -e "${YELLOW}$RESPONSE${NC}"
  fi
  sleep 1
done

echo ""
echo "Final circuit breaker state..."
CB_STATE=$(curl -s http://localhost:$ORDER_PORT/actuator/health | jq -r '.components.circuitBreakers.details.kitchenService.details.state')
echo -e "Circuit Breaker State: ${YELLOW}$CB_STATE${NC}"

if [ "$CB_STATE" == "CLOSED" ]; then
  echo -e "${GREEN}✓ Circuit breaker is CLOSED (recovered!)${NC}"
else
  echo -e "${YELLOW}⚠ Circuit breaker is in $CB_STATE state${NC}"
fi

echo ""
echo "========================================="
echo "Test 3: Circuit Breaker Metrics"
echo "========================================="

echo "Order Service - kitchenService circuit breaker metrics:"
curl -s http://localhost:$ORDER_PORT/actuator/metrics/resilience4j.circuitbreaker.calls | jq '.measurements[] | select(.statistic == "COUNT") | .value'

echo ""
echo "Billing Service - orderService circuit breaker metrics:"
curl -s http://localhost:$BILLING_PORT/actuator/metrics/resilience4j.circuitbreaker.calls | jq '.measurements[] | select(.statistic == "COUNT") | .value'

echo ""
echo "========================================="
echo "Test Complete!"
echo "========================================="
