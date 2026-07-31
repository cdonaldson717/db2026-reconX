// TICKET-ADV112-related — fetch wrapper that attaches Bearer JWT from sessionStorage.
const BASE = '/api';

function authHeaders() {
  const token = sessionStorage.getItem('reconx-token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(method, path, body) {
  const response = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    const contentType = response.headers.get('content-type') || '';
    const errorBody = contentType.includes('application/json')
      ? await response.json()
      : await response.text();
    const detail = typeof errorBody === 'string'
      ? errorBody
      : errorBody.detail || errorBody.message || response.statusText;
    throw new Error(`HTTP ${response.status}: ${detail}`);
  }

  return response.status === 204 ? null : response.json();
}

export const api = {
  login: (email, password)   => {
    // TODO(TICKET-ADV072): POST /auth/login with { email, password }.
    throw new Error('TICKET-ADV072 not implemented');
  },
  listTrades: (params = '')  => {
    // TODO(TICKET-ADV114): GET /v1/trades + `params` query string.
    throw new Error('TICKET-ADV114 not implemented');
  },
  createTrade: (req)         => request('POST', '/v1/trades', req),
  updateStatus: (id, status) => {
    // TODO(TICKET-ADV119): PATCH /v1/trades/{id}/status with { status }.
    throw new Error('TICKET-ADV119 not implemented');
  },
  deleteTrade: (id)          => {
    // TODO(TICKET-ADV119): DELETE /v1/trades/{id}.
    throw new Error('TICKET-ADV119 not implemented');
  },
  runRecon: (req)            => {
    // TODO(TICKET-ADV121): POST /v1/recon/run to enqueue a recon job.
    throw new Error('TICKET-ADV121 not implemented');
  },
  reconResults: (jobId)      => {
    // TODO(TICKET-ADV121): GET /v1/recon/jobs/{jobId}/results.
    throw new Error('TICKET-ADV121 not implemented');
  },
  audit: (tradeRef)          => {
    // TODO(TICKET-ADV121): GET /v1/audit/trades/{tradeRef}.
    throw new Error('TICKET-ADV121 not implemented');
  },
};
