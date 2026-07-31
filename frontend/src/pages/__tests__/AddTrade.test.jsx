import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AddTrade } from '../AddTrade.jsx';
import { api } from '@services/apiService.js';

vi.mock('@services/apiService.js', () => ({
  api: {
    createTrade: vi.fn(),
  },
}));

describe('<AddTrade>', () => {
  beforeEach(() => {
    api.createTrade.mockReset();
  });

  it('shows a message for every invalid field and does not submit', async () => {
    const user = userEvent.setup();
    render(<AddTrade />);

    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(await screen.findAllByRole('alert')).toHaveLength(8);
    expect(api.createTrade).not.toHaveBeenCalled();
  });

  it('submits valid values with numeric fields converted to numbers', async () => {
    const user = userEvent.setup();
    api.createTrade.mockResolvedValue({ id: 1 });
    render(<AddTrade />);

    await user.type(screen.getByLabelText('Trade reference'), 'EQU-20260315-0001');
    await user.type(screen.getByLabelText('Instrument ID'), '12');
    await user.type(screen.getByLabelText('Counterparty ID'), '34');
    await user.selectOptions(screen.getByLabelText('Asset class'), 'EQUITY');
    await user.selectOptions(screen.getByLabelText('Side'), 'BUY');
    await user.type(screen.getByLabelText('Quantity'), '1000');
    await user.type(screen.getByLabelText('Price'), '245.50');
    await user.type(screen.getByLabelText('Trade date'), '2026-03-15');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    await waitFor(() => {
      expect(api.createTrade).toHaveBeenCalledWith({
        tradeRef: 'EQU-20260315-0001',
        instrumentId: 12,
        counterpartyId: 34,
        assetClass: 'EQUITY',
        side: 'BUY',
        quantity: 1000,
        price: 245.5,
        tradeDate: '2026-03-15',
      });
    });
  });
});
