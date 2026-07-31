import React from 'react';

function TradeRowImpl({ trade, onClick, isSelected = false }) {
  const statusClass = String(trade.status ?? '')
    .toLowerCase()
    .replaceAll('_', '-');

  const symbol = trade.instrumentSymbol ?? trade.symbol ?? '';
  const quantity = trade.quantity ?? trade.qty ?? '';
  const id = trade.id ?? trade.tradeRef;

  function handleClick() {
    onClick?.(id);
  }

  return (
    <div
      className={`data-table__row${isSelected ? ' data-table__row--selected' : ''}`}
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
      <span role="cell">{symbol}</span>
      <span role="cell">{quantity}</span>
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
    (previous.trade.instrumentSymbol ?? previous.trade.symbol) ===
      (next.trade.instrumentSymbol ?? next.trade.symbol) &&
    (previous.trade.quantity ?? previous.trade.qty) ===
      (next.trade.quantity ?? next.trade.qty) &&
    previous.trade.price === next.trade.price &&
    previous.trade.status === next.trade.status &&
    previous.isSelected === next.isSelected &&
    previous.onClick === next.onClick
  );
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);

export default TradeRow;
