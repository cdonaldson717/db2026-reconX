import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  const statusClass = String(trade.status ?? '')
    .toLowerCase()
    .replaceAll('_', '-');

  function handleClick() {
    onClick?.(trade.id);
  }

  return (
    <div
      className="data-table__row"
      role="row"
      onClick={handleClick}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          handleClick();
        }
      }}
      tabIndex={onClick ? 0 : undefined}
    >
      <span role="cell">{trade.tradeRef}</span>
      <span role="cell">{trade.symbol}</span>
      <span role="cell">{trade.qty}</span>
      <span role="cell">{trade.price}</span>
      <span role="cell">
        <span className={`status-pill status-pill--${statusClass}`}>
          {trade.status}
        </span>
      </span>
    </div>
  );
}

function areEqual(previous, next) {
  return (
    previous.trade.id === next.trade.id &&
    previous.trade.tradeRef === next.trade.tradeRef &&
    previous.trade.symbol === next.trade.symbol &&
    previous.trade.qty === next.trade.qty &&
    previous.trade.price === next.trade.price &&
    previous.trade.status === next.trade.status &&
    previous.onClick === next.onClick
  );
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);

export default TradeRow;