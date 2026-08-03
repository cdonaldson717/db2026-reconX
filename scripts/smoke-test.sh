#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL=${BACKEND_URL:-http://localhost:8080/api}
PROMETHEUS_URL=${PROMETHEUS_URL:-http://localhost:9090}
GRAFANA_URL=${GRAFANA_URL:-http://localhost:3000}
GRAFANA_USER=${GRAFANA_USER:-admin}
GRAFANA_PASSWORD=${GRAFANA_PASSWORD:-admin}

fail() {
  echo "  ✗ $1 FAILED" >&2
  exit 1
}

for command in docker curl jq; do
  command -v "$command" >/dev/null 2>&1 || fail "required command '${command}' is unavailable"
done

echo "[step 1/7] Starting the stack"
if [[ "${RESET_STACK:-false}" == "true" ]]; then
  docker compose down -v >/dev/null 2>&1 || true
fi
docker compose up -d --build

backend_status="starting"
for ((attempt = 1; attempt <= 18; attempt += 1)); do
  backend_status=$(docker inspect --format='{{.State.Health.Status}}' reconx-backend 2>/dev/null || echo "starting")
  [[ "$backend_status" == "healthy" ]] && break
  sleep 5
done
[[ "$backend_status" == "healthy" ]] || fail "backend healthcheck"
echo "  ✓ stack is ready"

echo "[step 2/7] Logging in"
login_response=$(curl -fsS -X POST "${BACKEND_URL}/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"trader@db.com","password":"trader123"}') || fail "login"
token=$(jq -er '.token | select(length > 0)' <<<"$login_response") || fail "login token"
echo "  ✓ JWT acquired"

echo "[step 3/7] Creating a trade"
trade_ref="SMK-$(date +%Y%m%d)-$(printf '%04d' $((RANDOM % 10000)))"
trade_date=$(date +%Y-%m-%d)
trade_payload=$(jq -n \
  --arg tradeRef "$trade_ref" \
  --arg tradeDate "$trade_date" \
  '{tradeRef:$tradeRef,instrumentId:1,counterpartyId:1,assetClass:"EQUITY",side:"BUY",quantity:100,price:245.5,tradeDate:$tradeDate}')
trade_response=$(curl -fsS -X POST "${BACKEND_URL}/v1/trades" \
  -H "Authorization: Bearer ${token}" \
  -H 'Content-Type: application/json' \
  -d "$trade_payload") || fail "trade POST"
trade_id=$(jq -er '.id' <<<"$trade_response") || fail "trade response"
echo "  ✓ trade ${trade_id} created (${trade_ref})"

echo "[step 4/7] Confirming the Kafka event"
sleep 3
kafka_events=$(docker exec reconx-kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic trade-events \
  --from-beginning \
  --timeout-ms 10000 2>/dev/null || true)
grep -Fq "$trade_ref" <<<"$kafka_events" || fail "Kafka event"
echo "  ✓ trade event found"

echo "[step 5/7] Confirming the audit row"
audit_count="0"
for ((attempt = 1; attempt <= 10; attempt += 1)); do
  audit_count=$(docker exec reconx-postgres psql -U reconx_user -d reconx -tAc \
    "SELECT COUNT(*) FROM audit_log WHERE trade_ref='${trade_ref}';" | tr -d '[:space:]')
  [[ "$audit_count" =~ ^[1-9][0-9]*$ ]] && break
  sleep 2
done
[[ "$audit_count" =~ ^[1-9][0-9]*$ ]] || fail "Postgres audit row"
echo "  ✓ audit row present"

echo "[step 6/7] Confirming the Prometheus target"
prometheus_response=$(curl -fsS -G "${PROMETHEUS_URL}/api/v1/query" \
  --data-urlencode 'query=up{job="reconx-backend"}') || fail "Prometheus query"
jq -e '.data.result | any(.value[1] == "1")' <<<"$prometheus_response" >/dev/null \
  || fail "Prometheus target"
echo "  ✓ Prometheus is scraping the backend"

echo "[step 7/7] Confirming the Grafana datasource"
grafana_response=$(curl -fsS -u "${GRAFANA_USER}:${GRAFANA_PASSWORD}" \
  "${GRAFANA_URL}/api/datasources/uid/reconx-prometheus") || fail "Grafana datasource request"
jq -e '.uid == "reconx-prometheus"' <<<"$grafana_response" >/dev/null \
  || fail "Grafana datasource"
echo "  ✓ Grafana datasource is provisioned"

echo "All 7 checks green — stack is demo-ready."
