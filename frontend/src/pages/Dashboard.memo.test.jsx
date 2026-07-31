import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthContext } from '@context/AuthContext.jsx';
import { ThemeProvider } from '@context/ThemeContext.jsx';
import Dashboard from './Dashboard.jsx';

const { useTradeStreamMock } = vi.hoisted(() => ({
  useTradeStreamMock: vi.fn(() => ({ trades: [], isConnected: false })),
}));

vi.mock('@hooks/useTradeStream.js', () => ({ useTradeStream: useTradeStreamMock }));

const trades = [
  { id: 1, quantity: 100, price: 250, status: 'MATCHED' },
];

function Providers({ children }) {
  return (
    <AuthContext.Provider value={{ user: { role: 'TRADER' }, isLoading: false }}>
      <ThemeProvider>
        <MemoryRouter>{children}</MemoryRouter>
      </ThemeProvider>
    </AuthContext.Provider>
  );
}

describe('memoized Dashboard', () => {
  beforeEach(() => {
    useTradeStreamMock.mockClear();
  });

  it('preserves the dashboard subtree across an unrelated parent rerender', () => {
    const { rerender } = render(
      <Providers><Dashboard trades={trades} /></Providers>,
    );
    const portfolioHeading = screen.getByRole('heading', { name: /portfolio value/i });
    expect(useTradeStreamMock).toHaveBeenCalledOnce();

    rerender(<Providers><Dashboard trades={trades} /></Providers>);

    expect(useTradeStreamMock).toHaveBeenCalledOnce();
    expect(screen.getByRole('heading', { name: /portfolio value/i })).toBe(portfolioHeading);
    expect(screen.getByText('25,000')).toBeInTheDocument();
  });
});
