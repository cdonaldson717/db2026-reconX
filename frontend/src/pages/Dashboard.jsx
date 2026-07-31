import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';

const StatCard = React.memo(function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
    </article>
  );
});

export function Dashboard({ trades: providedTrades }) {
  const { trades: streamedTrades, isConnected } = useTradeStream();
  const trades = providedTrades ?? streamedTrades;

  const portfolioValue = useMemo(
    () =>
      trades.reduce((total, trade) => {
        const quantity = Number(trade.quantity) || 0;
        const price = Number(trade.price) || 0;

        return total + quantity * price;
      }, 0),
    [trades]
  );

  const tradeSummary = useMemo(() => {
    return trades.reduce(
      (summary, trade) => {
        const status = String(trade.status ?? '').toUpperCase();
        const quantity = Number(trade.quantity) || 0;
        const price = Number(trade.price) || 0;
        const value = quantity * price;

        if (status === 'MATCHED') {
          summary.matched += 1;
          summary.matchedValue += value;
        } else if (status === 'UNMATCHED') {
          summary.unmatched += 1;
        } else if (status === 'DISPUTED') {
          summary.disputed += 1;
        }

        return summary;
      },
      {
        matched: 0,
        unmatched: 0,
        disputed: 0,
        matchedValue: 0,
      }
    );
  }, [trades]);

  const openBreaks =
    tradeSummary.unmatched + tradeSummary.disputed;

  return (
    <section>
      <h2>Dashboard</h2>

      <div className="stat-grid">
        <StatCard
          label="Portfolio value (USD)"
          value={portfolioValue.toLocaleString()}
        />

        <StatCard
          label="Trades streamed"
          value={trades.length}
        />

        <StatCard
          label="Matched"
          value={tradeSummary.matched}
        />

        <StatCard
          label="Open breaks"
          value={openBreaks}
        />
      </div>

      <div role="status" aria-live="polite">
        SSE: {isConnected ? 'connected' : 'disconnected'}
      </div>
    </section>
  );
}

const MemoizedDashboard = React.memo(Dashboard);

export default withAuth(MemoizedDashboard);
