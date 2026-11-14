#!/usr/bin/env bash
set -e

echo "============================================="
echo "   APEX-Core Docker Startup Automation       "
echo "============================================="

# 1. Check if Docker daemon is running
if ! docker info >/dev/null 2>&1; then
    echo ">> [1/3] Docker Desktop daemon is offline. Starting Docker Desktop..."
    cmd.exe /c "start \"\" \"C:\Program Files\Docker\Docker\Docker Desktop.exe\"" >/dev/null 2>&1

    echo -n ">> [2/3] Waiting for Docker Engine to be ready"
    MAX_RETRIES=40
    COUNT=0
    until docker info >/dev/null 2>&1; do
        COUNT=$((COUNT+1))
        if [ $COUNT -ge $MAX_RETRIES ]; then
            echo ""
            echo ">> [ERROR] Docker Engine did not initialize in time. Please check Docker Desktop manually."
            exit 1
        fi
        sleep 2
        echo -n "."
    done
    echo ""
    echo ">> [SUCCESS] Docker Engine is online and responsive!"
else
    echo ">> [1/2] Docker Engine is already active."
fi

# 2. Start services via Docker Compose
echo ">> Starting infrastructure services (MySQL 8.0, Redis 7, Redpanda)..."
docker compose -f deploy/docker-compose.yml up -d

echo ""
echo ">> [ACTIVE CONTAINERS]"
docker compose -f deploy/docker-compose.yml ps
echo "============================================="
echo "Infrastructure is ready! You can now start the backend services."

