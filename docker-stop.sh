#!/usr/bin/env bash

echo "============================================="
echo "   APEX-Core Docker Shutdown Automation      "
echo "============================================="

# 1. Stop all containers cleanly (preserves volume data)
echo ">> [1/2] Stopping containers (preserving database data)..."
docker compose -f deploy/docker-compose.yml stop

echo ""
echo ">> [SUCCESS] Containers safely stopped."
echo ""

# 2. Optional: Shutdown Docker Desktop to release host RAM
read -p ">> [2/2] Do you also want to shut down Docker Desktop to free RAM? (y/N): " close_choice
if [[ "$close_choice" =~ ^[Yy]$ ]]; then
    echo ">> Closing Docker Desktop processes..."
    taskkill //F //IM "Docker Desktop.exe" >/dev/null 2>&1 || true
    taskkill //F //IM "com.docker.backend.exe" >/dev/null 2>&1 || true
    echo ">> [SUCCESS] Docker Desktop closed and host RAM freed."
else
    echo ">> Docker Desktop remains running in background."
fi
echo "============================================="

