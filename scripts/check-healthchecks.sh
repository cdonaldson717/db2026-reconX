#!/usr/bin/env bash
set -euo pipefail

wait_for_health() {
  local service=$1
  local container=$2
  local attempts=$3
  local delay=$4
  local status="starting"

  echo "Checking ${service} health..."
  docker compose up -d "$service"

  for ((attempt = 1; attempt <= attempts; attempt += 1)); do
    status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "starting")
    if [[ "$status" == "healthy" ]]; then
      echo "  ✓ ${service} healthy"
      return 0
    fi
    sleep "$delay"
  done

  echo "  ✗ ${service} did not become healthy (last status: ${status})" >&2
  docker inspect --format='{{json .State.Health}}' "$container" 2>/dev/null || true
  docker logs --tail 50 "$container" 2>/dev/null || true
  return 1
}

docker compose build backend frontend

wait_for_health postgres reconx-postgres 10 1
wait_for_health zookeeper reconx-zookeeper 10 1
wait_for_health kafka reconx-kafka 15 2
wait_for_health backend reconx-backend 20 3
wait_for_health frontend reconx-frontend 10 1
wait_for_health prometheus reconx-prometheus 10 1
wait_for_health grafana reconx-grafana 15 2

echo "All required healthchecks are green."
