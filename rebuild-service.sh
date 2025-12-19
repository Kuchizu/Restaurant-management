#!/bin/bash
# 🔄 Quick rebuild and restart a SINGLE service
# Usage: ./rebuild-service.sh menu-service

if [ -z "$1" ]; then
    echo "Usage: ./rebuild-service.sh <service-name>"
    echo "Example: ./rebuild-service.sh menu-service"
    exit 1
fi

SERVICE_NAME=$1

export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

echo "🔄 Rebuilding $SERVICE_NAME..."
docker-compose up --build -d --no-deps $SERVICE_NAME

echo "✅ Done! Checking status..."
docker-compose ps $SERVICE_NAME
docker-compose logs --tail=50 -f $SERVICE_NAME
