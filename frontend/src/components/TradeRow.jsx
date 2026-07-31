// TICKET-ADV119 / ADV121 - memoized trade row with stable onClick support.
import React from 'react';

function TradeRowImpl({ trade, onClick, isSelected = false }) {
  const id = trade.id ?? trade.tradeRef;
  const symbol = trade.instrumentSymbol ?? trade.symbol ?? '';

  return (
    <button
      type="button"
      className={`trade-row${isSelected ? ' trade-row--selected' : ''}`}
      onClick={() => onClick(id)}
    >
      <span>{trade.tradeRef}</span>
      <span>{symbol}</span>
      <span>{trade.quantity}</span>
      <span>{trade.price}</span>
      <span>{trade.status}</span>
    </button>
  );
}

function areEqual(prev, next) {
  return prev.trade.id === next.trade.id
    && prev.trade.tradeRef === next.trade.tradeRef
    && (prev.trade.instrumentSymbol ?? prev.trade.symbol) === (next.trade.instrumentSymbol ?? next.trade.symbol)
    && prev.trade.quantity === next.trade.quantity
    && prev.trade.price === next.trade.price
    && prev.trade.status === next.trade.status
    && prev.isSelected === next.isSelected
    && prev.onClick === next.onClick;
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);
