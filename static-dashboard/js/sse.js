// TICKET-ADV104 — browser-managed Server-Sent Events subscription.
(function () {
  const STREAM_URL = '/api/v1/trades/stream';
  const FEED_EL = document.getElementById('trade-feed');
  const STATUS_EL = document.getElementById('sse-status');

  if (!FEED_EL || !STATUS_EL) return;

  let sse = null;
  let connectionStatus = 'connecting';

  function updateConnectionBadge(text, variant) {
    connectionStatus = variant;
    STATUS_EL.textContent = text;
    STATUS_EL.className = `sse-status sse-status--${connectionStatus}`;
  }

  function escapeHtml(value) {
    const entities = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;',
    };

    return String(value ?? '').replace(/[&<>"']/g, (character) => entities[character]);
  }

  const formatQty = new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 4,
  });
  const formatPrice = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  });

  function formatNumber(formatter, value) {
    const number = Number(value);
    return Number.isFinite(number) ? formatter.format(number) : '—';
  }

  function prependTradeRow(trade) {
    const status = String(trade.status ?? '').toUpperCase();
    const statusModifier = {
      MATCHED: 'trade-card--matched',
      UNMATCHED: 'trade-card--break',
      DISPUTED: 'trade-card--break',
      BREAK: 'trade-card--break',
    }[status] ?? '';

    const row = document.createElement('article');
    row.className = ['trade-card', statusModifier, 'trade-card--new']
      .filter(Boolean)
      .join(' ');
    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${escapeHtml(trade.tradeRef)}</strong>
        <span>${escapeHtml(status)}</span>
      </header>
      <div class="trade-card__body">
        <span>${escapeHtml(trade.instrumentSymbol ?? trade.symbol)}</span>
        <span>Qty ${formatNumber(formatQty, trade.quantity ?? trade.qty)}</span>
        <span>Price ${formatNumber(formatPrice, trade.price)}</span>
        <span>${escapeHtml(trade.currency)}</span>
      </div>`;

    FEED_EL.prepend(row);

    window.setTimeout(function () {
      row.classList.remove('trade-card--new');
    }, 500);

    while (FEED_EL.children.length > 50) {
      FEED_EL.lastElementChild.remove();
    }
  }

  function connect() {
    updateConnectionBadge('Connecting…', 'connecting');
    sse = new EventSource(STREAM_URL);

    sse.onopen = function () {
      updateConnectionBadge('Live', 'live');
    };

    sse.onmessage = function (event) {
      try {
        prependTradeRow(JSON.parse(event.data));
      } catch (error) {
        console.error('Ignoring malformed trade event', error);
      }
    };

    sse.onerror = function () {
      // EventSource reconnects automatically; do not create another instance.
      updateConnectionBadge('Reconnecting…', 'reconnecting');
    };
  }

  window.addEventListener('beforeunload', function () {
    sse?.close();
  });

  connect();
})();
