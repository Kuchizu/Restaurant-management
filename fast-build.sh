#!/bin/bash
# 🚀 ULTRA-FAST Docker Build & Run Script
# ⚡ Uses BuildKit, parallel builds, and caching

set -e

echo "🚀 Starting ULTRA-FAST build..."
echo "================================"

# Enable BuildKit for parallel builds and better caching
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Clean old containers but keep volumes (preserve DB data)
echo "🧹 Cleaning old containers..."
docker-compose down --remove-orphans

# Build and start all services in parallel
echo "🔨 Building and starting services (parallel mode)..."
docker-compose up --build -d --parallel

echo ""
echo "✅ Build complete!"
echo "================================"
echo "📊 Checking service status..."
echo ""

# Wait a bit for services to initialize
sleep 5

# Show status
docker-compose ps

echo ""
echo "🌐 Services available at:"
echo "  - API Gateway:     http://localhost:49999"
echo "  - Eureka:          http://localhost:8761"
echo "  - Config Server:   http://localhost:8888"
echo "  - Order Service:   http://localhost:8081"
echo "  - Kitchen Service: http://localhost:8082"
echo "  - Menu Service:    http://localhost:8083 (WebFlux + JPA)"
echo "  - Inventory:       http://localhost:8084"
echo "  - Billing:         http://localhost:8085"
echo ""
echo "📝 View logs: docker-compose logs -f [service-name]"
echo "🛑 Stop all:  docker-compose down"
echo ""
echo "⏳ Services starting... Full startup in ~2-3 minutes"
