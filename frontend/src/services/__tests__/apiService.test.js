import { afterEach, describe, expect, it, vi } from 'vitest';
import { api } from '../apiService.js';

describe('api.createTrade', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    sessionStorage.clear();
  });

  it('posts the trade as JSON to /api/v1/trades', async () => {
    const trade = {
      tradeRef: 'EQU-20260315-0001',
      instrumentId: 12,
      counterpartyId: 34,
      assetClass: 'EQUITY',
      side: 'BUY',
      quantity: 1000,
      price: 245.5,
      tradeDate: '2026-03-15',
    };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      json: vi.fn().mockResolvedValue({ id: 1, ...trade }),
    });
    vi.stubGlobal('fetch', fetchMock);
    sessionStorage.setItem('reconx-token', 'test-token');

    await api.createTrade(trade);

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/trades', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer test-token',
      },
      body: JSON.stringify(trade),
    });
  });
});
