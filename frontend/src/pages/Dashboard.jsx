// TICKET-ADV120 — useMemo for portfolio-value calc.
// TICKET-ADV116 — useTradeStream live feed.
import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
    </article>
  );
}

export function Dashboard({ trades: providedTrades }) {
  const { trades: streamedTrades, isConnected } = useTradeStream();
  const trades = providedTrades ?? streamedTrades;
  const summary = useMemo(() => trades.reduce(
    (totals, trade) => ({
      portfolioValue: totals.portfolioValue + Number(trade.quantity) * Number(trade.price),
      matched: totals.matched + (trade.status === 'MATCHED' ? 1 : 0),
      unmatched: totals.unmatched + (
        ['UNMATCHED', 'DISPUTED'].includes(trade.status) ? 1 : 0
      ),
    }),
    { portfolioValue: 0, matched: 0, unmatched: 0 },
  ), [trades]);

  return (
    <section>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Portfolio value" value={summary.portfolioValue.toLocaleString('en-US')} />
        <StatCard label="Trades streamed" value={trades.length.toLocaleString('en-US')} />
        <StatCard label="Matched trades" value={summary.matched.toLocaleString('en-US')} />
        <StatCard label="Unmatched trades" value={summary.unmatched.toLocaleString('en-US')} />
      </div>
      <div role="status" aria-live="polite">
        SSE: {isConnected ? 'connected' : 'disconnected'}
      </div>
    </section>
  );
}

export default withAuth(Dashboard);
