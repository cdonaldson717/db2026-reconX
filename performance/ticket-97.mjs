import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api';
const tradeCreationDuration = new Trend('trade_creation_duration', true);
const tradeCreationSuccesses = new Counter('trade_creation_successes');
const tradeCreationFailures = new Counter('trade_creation_failures');

export const options = {
  scenarios: {
    tradeCreation: {
      executor: 'shared-iterations',
      vus: 10,
      iterations: 100,
      maxDuration: '1m',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
    trade_creation_duration: ['p(95)<200'],
    trade_creation_failures: ['count==0'],
  },
};

export function setup() {
  const loginResponse = http.post(
    `${baseUrl}/auth/login`,
    JSON.stringify({
      email: __ENV.RECONX_USERNAME || 'trader@db.com',
      password: __ENV.RECONX_PASSWORD || 'trader123',
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { endpoint: 'login' },
    },
  );

  const loggedIn = check(loginResponse, {
    'login returns 200': (response) => response.status === 200,
    'login returns a JWT': (response) => Boolean(response.json('token')),
  });

  if (!loggedIn) {
    exec.test.abort(`Login failed with HTTP ${loginResponse.status}: ${loginResponse.body}`);
  }

  const runOffset = Number(__ENV.RUN_OFFSET || Math.floor(Date.now() / 1000) % 9800);
  if (!Number.isInteger(runOffset) || runOffset < 0 || runOffset > 9900) {
    exec.test.abort('RUN_OFFSET must be an integer between 0 and 9900');
  }

  return { token: loginResponse.json('token'), runOffset };
}

export default function createTrade(data) {
  const sequence = data.runOffset + exec.scenario.iterationInTest;
  const tradeRef = `PER-20260315-${String(sequence).padStart(4, '0')}`;

  const response = http.post(
    `${baseUrl}/v1/trades`,
    JSON.stringify({
      tradeRef,
      instrumentId: 1,
      counterpartyId: 1,
      assetClass: 'EQUITY',
      side: 'BUY',
      quantity: 100,
      price: 245.5,
      tradeDate: '2026-03-15',
    }),
    {
      headers: {
        Authorization: `Bearer ${data.token}`,
        'Content-Type': 'application/json',
      },
      tags: { endpoint: 'create-trade' },
    },
  );

  const created = check(response, {
    'trade creation returns 201': (result) => result.status === 201,
  });

  tradeCreationDuration.add(response.timings.duration);
  if (created) {
    tradeCreationSuccesses.add(1);
    tradeCreationFailures.add(0);
  } else {
    tradeCreationSuccesses.add(0);
    tradeCreationFailures.add(1);
  }
}

export function handleSummary(data) {
  const requests = data.metrics.iterations.values.count;
  const failures = data.metrics.trade_creation_failures
    ? data.metrics.trade_creation_failures.values.count
    : 0;
  const summary = {
    requests,
    successfulCreations: data.metrics.trade_creation_successes.values.count,
    requestsPerSecond: data.metrics.iterations.values.rate,
    p95Milliseconds: data.metrics.trade_creation_duration.values['p(95)'],
    checkSuccessRate: data.metrics.checks.values.rate,
    requestFailureRate: failures / requests,
  };

  const report = [
    'TICKET-ADV097 performance test',
    `Requests: ${summary.requests}`,
    `Throughput: ${summary.requestsPerSecond.toFixed(2)} req/s`,
    `P95 latency: ${summary.p95Milliseconds.toFixed(2)} ms`,
    `Checks passed: ${(summary.checkSuccessRate * 100).toFixed(2)}%`,
    `Request failures: ${(summary.requestFailureRate * 100).toFixed(2)}%`,
    '',
  ].join('\n');

  return {
    stdout: report,
    'performance/ticket-97-results.json': JSON.stringify(summary, null, 2),
  };
}
